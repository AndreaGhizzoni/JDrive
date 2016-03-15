package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.fs.DetectedEvent;
import it.hackcaffebabe.jdrive.util.DateUtils;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
 * Configurator.getInstance().load();
 * Thread t = new Thread(Watcher.getInstance());
 * t.start();
 * }</pre>
 *
 */
public final class Watcher implements Runnable
{
    private static final Logger log = LogManager.getLogger(
        Watcher.class.getSimpleName()
    );
    private static final WatchEvent.Kind[] mod = {
        ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
    };

    // watcher base path
    private static Path WATCHED_DIR;
    // watcher data file
    private static Path WATCHED_DATA_FILE;
    private static final String WATCHED_DATA_FILE_NAME = ".jwatch";

    private static Watcher instance;
    private WatchService watcher;
    private final HashMap<WatchKey, Path> directories = new HashMap<>();
    private final HashSet<String> excludingFiles = new HashSet<>();

    private LinkedBlockingQueue<DetectedEvent> dispatchingQueue;

    /**
     * Retrieve the instance of Watcher with the default base path.
     * NB: Be sure that Configurator.getInstance().load() is called before to get
     * this instance.
     * @return {@link Watcher} instance.
     * @throws IOException if creation of watcher fail.
     */
    public static Watcher getInstance() throws IOException {
        if(instance == null) {
            instance = new Watcher();
        }
        return instance;
    }

    /* Constructor method. IOException if newWatchService() fail. */
    private Watcher() throws IOException{
        this.watcher = FileSystems.getDefault().newWatchService();
        log.info("Watch Service retrieved correctly from FS.");

        WATCHED_DIR = PathsUtil.createWatchedDirectory();
        log.debug("Watcher base path from Configurator: "+ WATCHED_DIR.toAbsolutePath());
        WATCHED_DATA_FILE = WATCHED_DIR.resolve(WATCHED_DATA_FILE_NAME);
        if( !WATCHED_DATA_FILE.toFile().exists() ) {
            WATCHED_DATA_FILE = Files.createFile(WATCHED_DATA_FILE);
        }

        // add here all the file name that watcher must exclude
        this.excludingFiles.add(WATCHED_DATA_FILE_NAME);
    }

//==============================================================================
//  METHOD
//==============================================================================
    /* method to walk down a path given recursively and meanwhile register all
    * the directory */
    private void registerDirectories(Path start) throws IOException {
        Files.walkFileTree(start, new WatchServiceAdder() );
    }

    /* create or update the Watcher data file in working directory. */
    private void updateWatcherDataFile() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(
                WATCHED_DATA_FILE.toFile()));
        String lineRead = in.readLine();
        in.close();
        if( lineRead != null ) { // file is not empty
            String d = DateUtils.fromLongToString( Long.valueOf(lineRead), null);
            log.info("Last update since " + d);
        }

        // then write the timestamp
        BufferedWriter out = new BufferedWriter(new FileWriter(
                WATCHED_DATA_FILE.toFile()));
        out.write( String.valueOf( new Date().getTime() ));
        out.newLine();
        out.close();
    }

    /**
     * This method close the current Watcher.
     * @throws IOException if something went wrong with closing procedure.
     */
    public void kill() throws IOException {
        this.watcher.close();
    }

//==============================================================================
//  SETTER
//==============================================================================
    /**
     * Set the dispatching queue for all the events detected by Watcher.
     * @param dq {@link java.util.concurrent.LinkedBlockingQueue} of
     * {@link DetectedEvent}
     */
    public void setDispatchingQueue( LinkedBlockingQueue<DetectedEvent> dq ){
        this.dispatchingQueue = dq;
    }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public void run() {
        try {
            if( this.dispatchingQueue == null )
                throw new InterruptedException("Dispatch Queue missing.");

            updateWatcherDataFile();
            // register the watched directory from Configurator in every case.
            registerDirectories(WATCHED_DIR);

            WatchKey key;
            WatchEvent.Kind<?> kind;
            Path objectDetected, context;
            File fileDetected;
            while( true ){
                //retrieve and remove the next watch key
                key = this.watcher.take();

                //get list of events for the watch key
                for( WatchEvent<?> watchEvent : key.pollEvents() ) {
                    //get the kind of event (create, modify, delete)
                    kind = watchEvent.kind();
                    //get the objectDetected for the event
                    context = ((WatchEvent<Path>) watchEvent).context();
                    objectDetected = directories.get(key).resolve(context);
                    fileDetected = objectDetected.toFile();

                    // check if is a file to exclude or event == OVERFLOW
                    boolean skipThisDetection =
                            this.excludingFiles.contains(
                                    fileDetected.getName()
                            ) || kind.equals(OVERFLOW);
                    if( skipThisDetection )
                        continue;

                    //dispatch detected object into queue
                    this.dispatchingQueue.put(
                            new DetectedEvent(kind, objectDetected)
                    );

                    // if event is CREATE and is a Directory, attach watcher to it
                    if( kind.equals(ENTRY_CREATE) &&
                            PathsUtil.isDirectory(objectDetected)) {
                        registerDirectories(objectDetected);
                    }
                }

                // reset the key. if key is associated to the root of Watcher
                // send some message and stop this thread.
                // otherwise, remove only the key associated with path.
                if( !key.reset() ){
                    Path removed = directories.remove(key);
                    if( removed.equals(WATCHED_DIR) ){
                        this.dispatchingQueue.put(
                                new DetectedEvent(null, null,
                                        "Root directory deleted. Watcher stop." )
                        );
                        break;
                    }
                }

                // update the .jwatch data file here because in case of removing
                // root of watcher, this update cause IOException .jwatch not
                // found
                updateWatcherDataFile();
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
            try {
                this.watcher.close();
                log.info("Watch service closed correctly.");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

//==============================================================================
//  INNER CLASS
//==============================================================================
    /* inner class that walk down a given path and register all the folder.*/
    private class WatchServiceAdder extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes a)
                throws IOException {
            WatchKey k = dir.register(watcher, mod);
            directories.put(k, dir);
            log.debug(String.format("Path %s saved by watcher.", dir));
            return FileVisitResult.CONTINUE;
        }
    }
}
