package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.fs.watcher.events.Error;
import it.hackcaffebabe.jdrive.fs.watcher.events.WatcherEvent;
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
 * Thread t = new Thread( Watcher.getInstance() );
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

    private LinkedBlockingQueue<WatcherEvent> dispatchingQueue;

    /**
     * Retrieve the instance of Watcher with the default base path.
     * @return {@link Watcher} instance.
     * @throws IOException if creation of watcher fail.
     */
    public static Watcher getInstance() throws IOException {
        log.info("Try to get a Watcher instance...");
        if( instance == null ) {
            instance = new Watcher();
        }
        return instance;
    }

    private Watcher() throws IOException{
        log.info("Try to get WatchService from FileSystem...");
        this.watcher = FileSystems.getDefault().newWatchService();
        log.info("Watch Service retrieved correctly from FileSystem.");

        createBasePathIfNotExists();
    }

    private void createBasePathIfNotExists() throws IOException {
        log.debug("Try to retrieve watcher base path from configurator...");
        Configurator configurator = Configurator.getInstance();
        String watcherBasePathAsString = (String)configurator.get(
                Keys.WATCHED_DIR
        );

        this.watcherBasePath = Paths.get( watcherBasePathAsString )
                .toAbsolutePath();
        if( !this.watcherBasePath.toFile().exists() ){
            Files.createDirectories( this.watcherBasePath );
        }
        log.debug("Watcher base path set: "+ this.watcherBasePath);
    }

    /** This method close the current Watcher. */
    public void kill() {
        // TODO rename this method to close() ?
        log.info("Try to kill Watch service...");
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
        try {
            if( this.dispatchingQueue == null )
                throw new InterruptedException("Dispatch Queue missing.");

            registerDirectories( this.watcherBasePath );

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
        }finally {
            this.kill();
        }
    }

    /* method to walk down a path given recursively and meanwhile watcher
     * register all the visitedPathsByWatcher. */
    private void registerDirectories( Path start ) throws IOException {
        WatchServiceAdder watchServiceAdder = new WatchServiceAdder();
        Files.walkFileTree( start, watchServiceAdder );
        this.directories.putAll( watchServiceAdder.visitedPathsByWatcher );
    }

    private void dispatchPollEventsFrom(WatchKey eventWatchKey )
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
    /* class that walk down a given path and set the watcher for all the
     * folders to rise a event when event listed below occur. */
    private class WatchServiceAdder extends SimpleFileVisitor<Path> {
        final WatchEvent.Kind[] kindEvents = {
                ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
        };
        Map<WatchKey, Path> visitedPathsByWatcher = new HashMap<>();

        @Override
        public FileVisitResult preVisitDirectory( Path dir,
                                                  BasicFileAttributes attrs )
                throws IOException {
            WatchKey watchKey = dir.register( watcher, kindEvents );
            visitedPathsByWatcher.put( watchKey, dir );
            log.debug( String.format("Path %s saved by watcher.", dir) );
            return FileVisitResult.CONTINUE;
        }
    }
}
