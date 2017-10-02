package it.hackcaffebabe.jdrive.remote.google;

import static it.hackcaffebabe.jdrive.Launcher.setPidToThreadContext;
import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.local.watcher.events.Create;
import it.hackcaffebabe.jdrive.local.watcher.events.Delete;
import it.hackcaffebabe.jdrive.local.watcher.events.Modify;
import it.hackcaffebabe.jdrive.local.watcher.events.Error;
import it.hackcaffebabe.jdrive.local.watcher.events.WatcherEvent;
import it.hackcaffebabe.jdrive.mapping.MappedFileSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * UpLoader is a runnable components that is responsible to take
 * {@link WatcherEvent} and execute it
 * remotely.
 */
public class UpLoader implements Runnable
{
    private static final Logger log = LogManager.getLogger();
    private LinkedBlockingQueue<WatcherEvent> eventsQueue;
    private DriveFileManager driveFileManager;
    private MappedFileSystem mappedFileSystem;

    /**
     * Instance a new UpLoader with a queue to take the events.
     * @param eventsQueue {@link java.util.concurrent.LinkedBlockingQueue} queue
     *        to take events from.
     * @throws Exception if something went wrong.
     */
    public UpLoader( LinkedBlockingQueue<WatcherEvent> eventsQueue ) throws Exception {
        this.eventsQueue = eventsQueue;
        this.driveFileManager = DriveFileManager.getInstance();
        this.mappedFileSystem = MappedFileSystem.getInstance();
        log.info("Uploader ready to start.");
    }

    @Override
    public void run() {
        setPidToThreadContext();
        log.info("Uploaded started.");

        try {
            boolean keepRunning = true;
            WatcherEvent detectedEvent;
            while( keepRunning ) {
                detectedEvent = eventsQueue.take();

                if( detectedEvent instanceof Error ) {
                    log.debug(((Error) detectedEvent).toString());
                    keepRunning = false;
                }else{
                    Path localFile = detectedEvent.getFile();
                    if( !mappedFileSystem.isAccessible(localFile) ){
                        log.debug(
                            "Event detected BUT skip it because path="
                            +localFile+" is not accessible."
                        );
                        continue;
                    }

                    if( detectedEvent instanceof Create ) {
                        Create createEvent = (Create) detectedEvent;
                        log.debug(createEvent.toString());
                        try {
                            File uploaded = driveFileManager.uploadFile( localFile );
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    } else if( detectedEvent instanceof Modify ) {
                        Modify modifyEvent = (Modify) detectedEvent;
                        log.debug(modifyEvent.toString());
                        try {
                            File updatedRemoteFile = driveFileManager
                                    .updateRemoteFile( localFile );
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    } else if( detectedEvent instanceof Delete ) {
                        Delete deleteEvent = (Delete) detectedEvent;
                        log.debug(deleteEvent.toString());
                        try {
                            driveFileManager.deleteRemoteFileFrom( localFile );
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }

            log.info("Uploader closing.");
        } catch (Exception e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
