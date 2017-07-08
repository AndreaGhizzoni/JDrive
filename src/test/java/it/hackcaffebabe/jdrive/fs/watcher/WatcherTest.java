package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.Constants;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.fs.watcher.events.WatcherEvent;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Test case for {@link it.hackcaffebabe.jdrive.fs.watcher.Watcher}
 */
public class WatcherTest
{
    private final LinkedBlockingQueue<WatcherEvent> queue = new LinkedBlockingQueue<>();
    private final static int SPAWN_COUNT = 10;
    private final static long POLL_TIMEOUT = 10;

    private Watcher watcher;
    private Path watcherBasePath;

    @Before
    public void setUp(){
        // I need the application home directory in order to create the
        // properties file
        buildApplicationHomeDirectoryOrFail();
        // create configurator to get Keys.WATCHED_BASE_PATH
        buildConfiguratorOrFail();

        Configurator configurator = Configurator.getInstance();
        watcherBasePath = Paths.get( (String)configurator.get(Keys.WATCHED_BASE_PATH) );
    }


    @Test
    public void testWatcher(){
        final Path watcherBasePath = this.watcherBasePath;

        watcher = buildWatcherOrFail();
        watcher.setDispatchingQueue(queue);
        new Thread(watcher).start();

        new Thread( () -> spawnFoldersUnder(watcherBasePath) ).start();
        checkNEventsFromQueue(
            "After spawning some folders I expect to get only Creation event " +
                    "from queue",
            SPAWN_COUNT,
            StandardWatchEventKinds.ENTRY_CREATE
        );

        new Thread( () -> createWriteAndDeleteFilesUnder( watcherBasePath ) ).start();
        checkNEventsFromQueue(
            "After creating, modify and deleting a file I expect to get " +
                    "Creation, Modification and Delete event",
            3*SPAWN_COUNT,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY,
            StandardWatchEventKinds.ENTRY_DELETE
        );
    }

    @After
    public void tearDown(){
        watcher.startClosingProcedure();
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
           Path cfgPath = Paths.get( Constants.APP_PROPERTIES_FILE );
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

    private void checkNEventsFromQueue( String msg, int numberOfEvents,
                                       WatchEvent.Kind... expectedEvents ){
        List<WatchEvent.Kind> listExpEvents = Arrays.asList( expectedEvents );
        WatcherEvent detectedEvent;
        String message;
        //int eventCount = 0;

        // TODO problem listed below:
        //      counting events from queue causing problems when testClasses
        //      task is called, otherwise if this class is tested independently
        //      no problem will be found. I suspect that is some kind of
        //      threading problem caused by JUnit test execution.

        try {
            while( (detectedEvent = queue.poll(POLL_TIMEOUT, TimeUnit.SECONDS )) != null ){
                //eventCount++;
                WatchEvent.Kind actualKindEvent = detectedEvent.Convert();
                message = msg.concat(": instead found " + actualKindEvent);
                Assert.assertTrue(message, listExpEvents.contains(actualKindEvent));
            }
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        //Assert.assertEquals("eventCount mismatch", numberOfEvents, eventCount);
    }

    private int spawnFoldersUnder( Path path ){
        String folderNamePattern = "folder%d";

        String folderName;
        Path folder;
        int counterOfFolderSpawned = 0;
        for(; counterOfFolderSpawned< SPAWN_COUNT; counterOfFolderSpawned++ ){
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

        String fileName;
        Path filePath;
        for( int i=0; i<SPAWN_COUNT; i++ ){
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
