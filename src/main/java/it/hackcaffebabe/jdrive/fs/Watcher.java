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
 */
public class Watcher implements Runnable
{
    private static final Logger log = LogManager.getLogger("Watcher");

    private Path dir = Paths.get("/home/andrea/test");//TODO move this outside
    private WatchService watcher;
    private final Map<WatchKey, Path> directories = new HashMap<WatchKey,Path>();
    private static final WatchEvent.Kind[] mod = {
            ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY
    };

    /**
     * TODO add doc
     * @throws IOException
     */
    public Watcher() throws IOException{
        this.watcher = FileSystems.getDefault().newWatchService();
    }

//    private void registerTree(Path start) throws IOException {
//        TODO check not null
//        Files.walkFileTree(start, new WatchServiceAdder() );
//    }
//
//    private void registerPath(Path path) throws IOException {
//        register the received path
//        WatchKey key = path.register(this.watcher, mod );
//        storeCredential the key and path
//        directories.put(key, path);
//    }

    private void register(Path start) throws IOException{
        boolean isDir = Files.isDirectory(start, LinkOption.NOFOLLOW_LINKS);
        if(isDir){
            Files.walkFileTree(start, new WatchServiceAdder() );
        }else{
            //register the received path
            WatchKey key = start.register(this.watcher, mod );
            //storeCredential the key and path
            directories.put(key, start);
        }
    }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public void run() {
        try {
            register(this.dir);
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
                        register(child);
                    }

                    log.info(kind + " -> " + child);
                }

                boolean valid = key.reset();
                //remove the key if it is not valid
                if( !valid ){
                    directories.remove(key);
                    if (directories.isEmpty())
                        break;
                }
            }

            this.watcher.close();

        }catch(InterruptedException inter){
            log.error("Interrupted Exit. "+inter.getMessage());
        }catch(IOException ioe){
            log.error("IOException Exit. "+ioe.getMessage());
        }
    }

//==============================================================================
//  INNER CLASS
//==============================================================================
    private class WatchServiceAdder extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            log.info("Registering:" + dir);
            register(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
