package it.hackcaffebabe.jdrive.local.watcher;

import static it.hackcaffebabe.jdrive.Launcher.setPidToThreadContext;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.local.watcher.events.Error;
import it.hackcaffebabe.jdrive.local.watcher.events.WatcherEvent;
import it.hackcaffebabe.jdrive.mapping.Mapper;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 * Watcher class is a singleton Object that can detect event from a base path.
 * The events that can detect are: create, delete and modify.
 *
 * How to use:
 * <pre>{@code
 * Paths.createApplicationHomeDirectory();
 * Configurator.setup( cfgPath )
 * Watcher w = Watcher.getInstance();
 * w.init();
 * Thread t = new Thread( w );
 * t.start();
 * }</pre>
 */
public final class Watcher implements Runnable
{
    private static final Logger log = LogManager.getLogger();
    private static Watcher instance;

    private Path watcherBasePath;

    private WatchService watcher;
    private Map<WatchKey, Path> directories = new HashMap<>();
    private Mapper mapper;

    private LinkedBlockingQueue<WatcherEvent> dispatchingQueue;

    /**
     * Retrieve the instance of Watcher with the default base path.
     * @return {@link Watcher} instance.
     */
    public static Watcher getInstance() {
        log.info("Try to get a Watcher instance...");
        if( instance == null ) {
            instance = new Watcher();
        }
        return instance;
    }

    private Watcher(){}

    /**
     * TODO add doc
     * @throws IOException
     */
    public Mapper init() throws IOException {
        log.info("Try to get WatchService from FileSystem...");
        this.watcher = FileSystems.getDefault().newWatchService();
        log.info("Watch Service retrieved correctly from FileSystem.");

        createBasePathIfNotExists();
        registerDirectoriesAndMap( this.watcherBasePath );
        return this.mapper;
    }

    private void createBasePathIfNotExists() throws IOException {
        log.debug("Try to retrieve watcher base path from configurator...");
        Configurator configurator = Configurator.getInstance();
        String watcherBasePathAsString = (String)configurator.get(
            Keys.WATCHED_BASE_PATH
        );

        if( watcherBasePathAsString == null ){
            throw new IOException(
                "Configurator is not set with Watcher base path."
            );
        }

        this.watcherBasePath = Paths.get( watcherBasePathAsString )
                .toAbsolutePath();
        if( !this.watcherBasePath.toFile().exists() ){
            Files.createDirectories( this.watcherBasePath );
        }
        log.debug("Watcher base path set: "+ this.watcherBasePath);
    }

    /** This method close the current Watcher. */
    public void startClosingProcedure() {
        try {
            this.watcher.close();
            log.info("Watch service closed correctly.");
        } catch (IOException e) {
            log.error( e.getMessage() );
        }
    }

    /**
     * Set the dispatching queue for all the WatcherEvent detected by Watcher.
     * @param queue {@link java.util.concurrent.LinkedBlockingQueue} of
     *              {@link WatcherEvent}
     */
    public void setDispatchingQueue( LinkedBlockingQueue<WatcherEvent> queue ){
        this.dispatchingQueue = queue;
    }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public void run() {
        setPidToThreadContext();

        try {
            if( this.dispatchingQueue == null )
                throw new InterruptedException("Dispatch Queue missing.");

            WatchKey detectedWatchKey;
            while( true ){
                // get the next event rise by File System as WatchKey
                detectedWatchKey = this.watcher.take();

                dispatchPollEventsFrom( detectedWatchKey );

                // reset the key. if key is associated to the root of Watcher,
                // send some message and stop this thread;  otherwise, remove
                // only the key associated with path.
                if( !detectedWatchKey.reset() ){
                    Path removed = directories.remove( detectedWatchKey );
                    if( removed.equals( this.watcherBasePath ) ){
                        this.dispatchingQueue.put(
                            new Error("Root directory deleted. Watcher stop.")
                        );
                        break;
                    }
                }
            }

        }catch(InterruptedException inter){
            log.error(inter.getMessage()+". Exit.");
        }catch(IOException ioe) {
            log.error("IOException Exit. " + ioe.getMessage());
        }catch (ClosedWatchServiceException closedWatcher){
            // this exception is thrown when the close() method is called when
            // the a pool() or take() method is waiting  for a key to be queued
            // DOCS: https://goo.gl/G02AEG
            try {
                this.dispatchingQueue.put( new Error( "Watcher is closing." ) );
            } catch (InterruptedException e) {
                log.error( e.getMessage() );
            }
        }finally {
            this.startClosingProcedure();
        }
    }

    /* method to walk down a path given recursively and meanwhile watcher
     * register all the directories. */
    private void registerDirectories( Path start ) throws IOException {
        WatcherFolderAdder watcherFolderAdder = new WatcherFolderAdder();
        Files.walkFileTree( start, watcherFolderAdder );
        this.directories.putAll( watcherFolderAdder.visitedPathsByWatcher );
    }

    /* method that walks down a path recursively while watcher records all
    *  directories and creates a Mapper object */
    private void registerDirectoriesAndMap( Path start ) throws IOException {
        WatcherAddedAndMapper watcherAddedAndMapper = new WatcherAddedAndMapper();
        Files.walkFileTree( start, watcherAddedAndMapper );
        this.directories.putAll( watcherAddedAndMapper.visitedPathsByWatcher );
        this.mapper = watcherAddedAndMapper.mapper;
    }

    private void dispatchPollEventsFrom( WatchKey eventWatchKey )
            throws InterruptedException, IOException {

        WatchEvent.Kind<?> eventKind;
        Path eventPath;
        for( WatchEvent<?> watchEvent : eventWatchKey.pollEvents() ){
            eventKind = watchEvent.kind();

            if( eventKind.equals(OVERFLOW) ) continue;

            eventPath = this.directories.get(eventWatchKey).resolve(
                ((WatchEvent<Path>) watchEvent).context()
            );

            //dispatch detected object into queue
            this.dispatchingQueue.put(
                WatcherEvent.BuildNewFrom( eventKind, eventPath )
            );

            // if event is CREATE and is a Directory, attach watcher to it
            if( eventKind.equals(ENTRY_CREATE) && PathsUtil.isDirectory(eventPath) ){
                registerDirectories( eventPath );
            }
        }
    }

//==============================================================================
//  INNER CLASS
//==============================================================================
    /* class that walks down a path recursively while watcher records all
    *  directories and creates a Mapper object */
    private class WatcherAddedAndMapper extends SimpleFileVisitor<Path> {
        final WatchEvent.Kind[] kindEvents = {
                ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
        };

        Map<WatchKey, Path> visitedPathsByWatcher = new HashMap<>();
        Mapper mapper = new Mapper();

        @Override
        public FileVisitResult preVisitDirectory( Path dir,
                                                  BasicFileAttributes attrs )
                                                  throws IOException {
            WatchKey watchKey = dir.register( watcher, kindEvents );
            visitedPathsByWatcher.put( watchKey, dir );
            log.debug( String.format("Path %s saved by watcher.", dir) );

            mapper.put( dir );
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile( Path file, BasicFileAttributes attr ) {
            mapper.put( file );
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed( Path file, IOException exc ) {
            log.error(exc.getMessage(), exc);
            return FileVisitResult.CONTINUE;
        }
    }

    /* class that walk down a given path and set the watcher for all the
     * folders in order to rise a event when event listed below occur. */
    private class WatcherFolderAdder extends SimpleFileVisitor<Path> {
        final WatchEvent.Kind[] kindEvents = {
                ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
        };
        Map<WatchKey, Path> visitedPathsByWatcher = new HashMap<>();

        @Override
        public FileVisitResult preVisitDirectory( Path dir,
                                                  BasicFileAttributes attrs )
                                                  throws IOException {
            WatchKey watchKey = dir.register(watcher, kindEvents);
            visitedPathsByWatcher.put( watchKey, dir );
            log.debug( String.format("Path %s saved by watcher.", dir) );
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile( Path file, BasicFileAttributes attr ) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed( Path file, IOException exc ) {
            log.error(exc.getMessage(), exc);
            return FileVisitResult.CONTINUE;
        }
    }
}
