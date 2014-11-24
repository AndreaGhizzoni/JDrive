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
            final Path dir = new File("/home/andrea/test").toPath();
            dir.register( watcher, mod );
            System.out.println("--- Starting watch on path "+dir);

            final SimpleFileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException {
                    dir.register(watcher, mod);
                    return FileVisitResult.CONTINUE;
                }
            };

            final SimpleFileVisitor<Path> registerExistingFiles = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                    if(d.equals(dir))
                        return FileVisitResult.CONTINUE;
                    d.register(watcher, mod);
                    System.out.println(d);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile( Path file, BasicFileAttributes attr) throws IOException {
                    System.out.println(file);
                    return FileVisitResult.CONTINUE;
                }
            };

            System.out.println("--- Existing files added to watcher services:");
            Files.walkFileTree(dir, registerExistingFiles);
            System.out.println("---");

            WatchKey key;
            while (true) {
                key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only for ENTRY_CREATE events,
                    // but an OVERFLOW event can occur regardless if events
                    // are lost or discarded.
                    if(kind == OVERFLOW) {
                        continue;
                    }

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path child = ((Path)key.watchable()).resolve(ev.context());
                    System.out.println( String.format("event from: %s", child) );

                    //this doesn't work
                    if(kind == ENTRY_DELETE) {
                        key.reset();
                        System.out.println("unregister service");
                        continue;
                    }

                    Boolean isFolder = child.toFile().isDirectory();
                    if( isFolder ) {
                        Files.walkFileTree(child, fileVisitor);
                        //process directory
                    }
                    System.out.println(String.format("=== key: %s - path: %s", kind, child));
                }

                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                if( !key.reset() ){
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
