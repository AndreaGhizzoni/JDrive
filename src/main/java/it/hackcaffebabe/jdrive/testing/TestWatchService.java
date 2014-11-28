package it.hackcaffebabe.jdrive.testing;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */
public class TestWatchService implements Runnable
{
    private WatchService watcher;
    private static final WatchEvent.Kind[] mod = {ENTRY_CREATE, ENTRY_DELETE,
                                                                ENTRY_MODIFY };
    private Path dir = Paths.get("/home/andrea/test");
    private final Map<WatchKey, Path> directories = new HashMap<WatchKey,Path>();

    public TestWatchService() throws IOException{
        this.watcher = FileSystems.getDefault().newWatchService();
    }

    private void registerTree(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                System.out.println("Registering:" + dir);
                registerPath(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void registerPath(Path path) throws IOException {
        //register the received path
        WatchKey key = path.register(this.watcher, mod );
        //store the key and path
        directories.put(key, path);
    }

    @Override
    public void run() {
        try {
            registerTree(this.dir);
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
                        if( Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS) )
                            registerTree(child);
                    }

//                    if( kind.equals(ENTRY_MODIFY) ){
//                    }

                    System.out.println(kind + " -> " + child);
                }

                boolean valid = key.reset();
                //remove the key if it is not valid
                if (!valid) {
                    directories.remove(key);
                    if (directories.isEmpty())
                        break;
                }
            }

            this.watcher.close();

        }catch(InterruptedException inter){
            System.err.println("Interrupted Exit.");
        }catch(IOException ioe){
            System.err.println("IOException Exit.");
        }
    }


    public static void main( String...args ){
        try {
            TestWatchService t = new TestWatchService();
            new Thread(t).start();
        }catch (IOException ioe){
            System.err.println("IOException Throw");
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
