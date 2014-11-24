package it.hackcaffebabe.jdrive;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * http://docs.oracle.com/javase/tutorial/essential/io/notification.html
 */
public class TestWatchService {

    public static void main( String...args ){
        try {
            final WatchService watcher = FileSystems.getDefault().newWatchService();
            final WatchEvent.Kind[] mod = {ENTRY_CREATE,
                                           ENTRY_DELETE,
                                           ENTRY_MODIFY };
            Path dir = new File("/home/andrea/test").toPath();
            dir.register( watcher, mod );

            final SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watcher, mod);
                return FileVisitResult.CONTINUE;
                }
            };

            WatchKey key ;
            while (true) {
                key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only for ENTRY_CREATE events,
                    // but an OVERFLOW event can occur regardless if events
                    // are lost or discarded.
                    if (kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path child = ((Path)key.watchable()).resolve(ev.context());
                    System.out.println( String.format("event from: %s", child ));

                    Boolean isFolder = child.toFile().isDirectory();
                    if( isFolder ){
                        if( kind == ENTRY_DELETE ){
                            key.reset();
                            key.cancel();
                            watcher.close();
                        }else {
                            Files.walkFileTree(child, fileVisitor);
                        }
                        //process directory
                    } else {
                        System.out.println(String.format("=== key: %s - path: %s", kind, child));
                    }
                }

                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                if( !key.reset() ){
                    key.cancel();
                    watcher.close();
                    break;
                }
            }
        }catch (NoSuchFileException nsfe){
            System.err.println("NoSuchFileException throw");
        } catch (IOException e) {
            e.printStackTrace();
        }catch (InterruptedException e){
            System.err.println("Interrupted Exit.");
        }
    }
}
