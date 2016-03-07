package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.fs.DetectedObject;
import it.hackcaffebabe.jdrive.util.DateUtils;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    private final ArrayList<String> excludedFile = new ArrayList<>();

    private LinkedBlockingQueue<DetectedObject> dispatchingQueue;

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
        this.excludedFile.add(WATCHED_DATA_FILE_NAME);
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

//==============================================================================
//  SETTER
//==============================================================================
    /**
     * Set the dispatching queue for all the events detected by Watcher.
     * @param dq {@link java.util.concurrent.LinkedBlockingQueue} of
     * {@link it.hackcaffebabe.jdrive.fs.DetectedObject}
     */
    public void setDispatchingQueue( LinkedBlockingQueue<DetectedObject> dq ){
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
            Path pathFileDetected, context;
            DetectedObject detObj;
            while( true ){
                //retrieve and remove the next watch key
                key = this.watcher.take();

                //get list of events for the watch key
                for( WatchEvent<?> watchEvent : key.pollEvents() ) {
                    //get the kind of event (create, modify, delete)
                    kind = watchEvent.kind();
                    //get the pathFileDetected for the event
                    context = ((WatchEvent<Path>) watchEvent).context();
                    pathFileDetected = directories.get(key).resolve(context);

                    // check if is a file to exclude.
                    if( this.excludedFile.contains(pathFileDetected.toFile().getName()) )
                        continue;
                    //handle OVERFLOW event
                    if( kind.equals(OVERFLOW) )
                        continue;

                    // ========== DEBUG OPTION!
                    if( pathFileDetected.toFile().getName().equals("exit") )
                        throw new InterruptedException("Controlled Exit.");
                    // ========== DEBUG OPTION!

                    //dispatch detected object into queue
                    File f = pathFileDetected.toFile();
                    detObj = new DetectedObject(
                        kind, f.getAbsolutePath(), f.lastModified()
                    );
                    this.dispatchingQueue.put(detObj);

                    // if event is CREATE and is a Directory, attach watcher to it
                    if( kind.equals(ENTRY_CREATE) &&
                            PathsUtil.isDirectory(pathFileDetected)) {
                        registerDirectories(pathFileDetected);
                    }

                    // update the .jwatch data file
                    updateWatcherDataFile();
                }

                if( !key.reset() )  directories.remove(key);
            }

        }catch(InterruptedException inter){
            log.error(inter.getMessage()+". Exit.");
        }catch(IOException ioe){
            log.error("IOException Exit. "+ioe.getMessage());
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
