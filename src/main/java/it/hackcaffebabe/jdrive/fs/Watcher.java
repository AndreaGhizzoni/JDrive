package it.hackcaffebabe.jdrive.fs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import static java.nio.file.StandardWatchEventKinds.*;
import static it.hackcaffebabe.jdrive.fs.WatcherUtil.*;

/**
 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */
public final class Watcher implements Runnable
{
    private static final Logger log = LogManager.getLogger("Watcher");

    private WatchService watcher;
    private final Map<WatchKey, Path> directories = new HashMap<WatchKey,Path>();
    private static final WatchEvent.Kind[] mod = {
            ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
    };

    private static Watcher instance;

    /**
     * TODO add docs
     * @return
     */
    public static Watcher getInstance() throws IOException {
        if(instance == null)
            instance = new Watcher();
        return instance;
    }

    /*
     * TODO add description
     * @throws IOException
     */
    private Watcher() throws IOException{
        log.entry();
        this.watcher = FileSystems.getDefault().newWatchService();
        log.info("Watch Service retrieved correctly from FS.");
    }

//==============================================================================
//  METHOD
//==============================================================================
    /* TODO add description */
    private void registerTree(Path start) throws IOException {
        Files.walkFileTree(start, new WatchServiceAdder() );
    }

    /* TODO add description */
    private void registerPath(Path path) throws IOException {
        //register the received path
        WatchKey key = path.register(this.watcher, mod );
        //storeCredential the key and path
        directories.put(key, path);
    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * TODO add description
     * @return
     */
    public Path getBase(){
        return BASE;
    }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public void run() {
        try {
            registerTree(BASE);
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
