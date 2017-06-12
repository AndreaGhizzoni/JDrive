package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.fs.watcher.events.WatcherEvent;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.junit.Assert;
import org.junit.Test;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Test case for {@link it.hackcaffebabe.jdrive.fs.watcher.Watcher}
 */
public class WatcherTest
{
    private LinkedBlockingQueue<WatcherEvent> queue = new LinkedBlockingQueue<>();

    @Test
    public void testWatcher(){
        // I need the application home directory in order to create the
        // properties file
        buildApplicationHomeDirectoryOrFail();
        // create configurator to get Keys.WATCHED_DIR
        buildConfiguratorOrFail();
        Configurator configurator = Configurator.getInstance();
        Path watcherBasePath = Paths.get( (String)configurator.get(Keys.WATCHED_DIR) );

        Watcher watcher = buildWatcherOrFail();
        watcher.setDispatchingQueue(queue);
        Thread watcherThread = new Thread(watcher);
        watcherThread.start();

        spawnFoldersUnder( watcherBasePath );
        checkEventsFromQueue(
            "After spawning some folders I expect to get only Creation event " +
                    "from queue",
            StandardWatchEventKinds.ENTRY_CREATE
        );

        createWriteAndDeleteFilesUnder( watcherBasePath );
        checkEventsFromQueue(
            "After creating, modify and deleting a file I expect to get " +
                    "Creation, Modification and Delete event",
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE
        );

        watcherThread.interrupt();
        deleteFolderAndContents( watcherBasePath );
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    private void buildApplicationHomeDirectoryOrFail(){
        try {
            PathsUtil.createApplicationHomeDirectory();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    private void buildConfiguratorOrFail(){
       try{
           Path cfgPath = Paths.get( PathsUtil.APP_CGF_FILE );
           Configurator.setup( cfgPath );
       } catch (Exception e) {
           Assert.fail(e.getMessage());
       }
    }

    private Watcher buildWatcherOrFail(){
        try {
           return Watcher.getInstance();
        } catch (IOException e) {
            Assert.fail("Fail to retrieve the Watcher. "+e.getMessage());
        }
        return null;
    }

    private void checkEventsFromQueue( String msg,
                                       WatchEvent.Kind... expectedEvents ){
        List<WatchEvent.Kind> listExpEvents = Arrays.asList( expectedEvents );
        WatcherEvent detectedEvent;
        String message;
        while( !queue.isEmpty() ){
            try {
                detectedEvent = queue.take();
                WatchEvent.Kind actualKindEvent = detectedEvent.Convert();

                message = msg.concat( ": instead found "+actualKindEvent );
                Assert.assertTrue( message, listExpEvents.contains(actualKindEvent) );
            } catch (InterruptedException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    private int spawnFoldersUnder( Path path ){
        String folderNamePattern = "folder%d";
        final int numberOfFolders = 10;

        String folderName;
        Path folder;
        int counterOfFolderSpawned = 1;
        for( ; counterOfFolderSpawned<=numberOfFolders; counterOfFolderSpawned++ ){
            folderName = String.format( folderNamePattern, counterOfFolderSpawned );
            folder = Paths.get( path.toString(), folderName );
            try {
                Files.createDirectories( folder );
            } catch (IOException e) {
                Assert.fail("Fail to spawn folders.");
            }
        }
        return counterOfFolderSpawned;
    }

    private void createWriteAndDeleteFilesUnder( Path path ){
        String fileNamePattern = "f%d.txt";
        final int numberOfFiles = 10;

        String fileName;
        Path filePath;
        for( int i=0; i<numberOfFiles; i++ ){
            try {
                fileName = String.format( fileNamePattern, i );
                filePath = Paths.get( path.toString(), fileName );
                Files.write(
                    filePath,
                    Collections.singletonList( "Hello I'm a new File!\n" )
                );
                Files.delete( filePath );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    private void deleteFolderAndContents( Path folder ){
        try {
            Files.walkFileTree( folder, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory( Path dir,
                                                          BasicFileAttributes attrs )
                        throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile( Path file,
                                                  BasicFileAttributes attrs )
                        throws IOException {
                    Files.delete( file );
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed( Path file,
                                                        IOException exc )
                        throws IOException {
                    Assert.fail(exc.getMessage());
                    return null;
                }

                @Override
                public FileVisitResult postVisitDirectory( Path dir,
                                                           IOException exc )
                        throws IOException {
                    Files.delete( dir );
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
