package it.hackcaffebabe.jdrive.fs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 * TODO add doc and examples
 */
public final class Watcher implements Runnable
{
    private static final Logger log = LogManager.getLogger("Watcher");

    private static Watcher instance;
    private WatchService watcher;
    private final Map<WatchKey, Path> directories = new HashMap<WatchKey,Path>();
    private static final WatchEvent.Kind[] mod = {
        ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
    };

    /**
     * Retrieve the instance of Watcher with the default base path.
     * @return {@link Watcher} instance.
     * @throws IOException if creation of watcher fail.
     */
    public static Watcher getInstance() throws IOException {
        if(instance == null)
            instance = new Watcher();
        return instance;
    }

    /**
     * Retrieve the instance of Watcher with a base path given. If Watcher is
     * not instanced yet then the new instance will have the given base path,
     * otherwise will be returned the instanced one.
     * @param base {@link java.nio.file.Path} the base path of directory.
     * @return {@link Watcher} instance.
     * @throws IOException if creation of watcher fail.
     */
    public static Watcher getInstance( Path base ) throws IOException {
        if(instance == null){
            WatcherUtil.setBase(base);
            instance = new Watcher();
        }
        return instance;
    }

    /* Constructor method. IOException if newWatchService() fail. */
    private Watcher() throws IOException{
        this.watcher = FileSystems.getDefault().newWatchService();
        log.info("Watch Service retrieved correctly from FS.");
    }

//==============================================================================
//  METHOD
//==============================================================================
    /* method to walk down a path given recursively and meanwhile register all
    * the directory */
    private void registerTree(Path start) throws IOException {
        Files.walkFileTree(start, new WatchServiceAdder() );
    }

    /* register the single path given as argument under the watcher service. */
    private void registerPath(Path path) throws IOException {
        WatchKey key = path.register(this.watcher, mod);
        directories.put(key, path);
        log.debug(String.format("Path %s saved by watcher.",path));
    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * TODO add doc
     */
    public synchronized int getNumberFolderWatched(){
        log.debug(this.directories.size());
        return this.directories.size();
    }

    /**
     * TODO add doc
     * @param p
     * @return
     */
    public synchronized boolean isPathWatched( Path p ){
        return this.directories.containsValue(p);
    }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public void run() {
        try {
            registerTree(WatcherUtil.getBase());
            WatchKey key;
            WatchEvent.Kind<?> kind;
            Path filename;
            while( true ){
                //retrieve and remove the next watch key
                key = this.watcher.take();

                //get list of events for the watch key
                for( WatchEvent<?> watchEvent : key.pollEvents() ) {
                    //get the kind of event (create, modify, delete)
                    kind = watchEvent.kind();

                    //get the filename for the event
                    filename = ((WatchEvent<Path>) watchEvent).context();

                    //handle OVERFLOW event
                    if( kind.equals(OVERFLOW) )
                        continue;

                    Path child = directories.get(key).resolve(filename);

                    //handle CREATE event
                    if( kind == ENTRY_CREATE ){
                        if(Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS))
                            registerTree(child);
                    }

                    log.info(kind + " -> " + child);
                }

                boolean valid = key.reset();
                //remove the key if it is not valid
                if( !valid ){
                    directories.remove(key);
                }
            }

        }catch(InterruptedException inter){
            log.error("Watcher interrupted. Exit.");
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
    /* inner class that walk down a given path and register all the folder. */
    private class WatchServiceAdder extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes a)
                throws IOException {
            log.info("Registering new path:" + dir);
            registerPath(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
