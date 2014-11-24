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
            final Path dir = Paths.get("/home/andrea/test");
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
            Path rPath;
            while (true) {
                key = watcher.take();
                if(!key.isValid()) {
                    System.out.println(String.format("Key %s is not valid", key));
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if( kind.equals(OVERFLOW) )
                        continue;

                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    rPath = ((Path)key.watchable()).resolve(ev.context());
                    System.out.println( String.format("resolved path: %s", rPath) );

                    //this doesn't work
                    if(kind.equals(ENTRY_DELETE) ) {
                        key.cancel();
                        System.out.println("unregister service");
                        continue;
                    }

                    boolean isFolder = rPath.toFile().isDirectory();
                    if( isFolder && kind.equals(ENTRY_CREATE) ) {
                        Files.walkFileTree(rPath, fileVisitor);
                        //process directory and continue
                    }
                    System.out.println(String.format("=== key: %s - path: %s", kind, rPath));
                    //process file
                }

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
