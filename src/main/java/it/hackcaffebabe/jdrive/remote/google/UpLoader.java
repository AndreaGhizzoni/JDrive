package it.hackcaffebabe.jdrive.remote.google;

import static it.hackcaffebabe.jdrive.Launcher.setPidToThreadContext;
import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.local.watcher.events.Create;
import it.hackcaffebabe.jdrive.local.watcher.events.Delete;
import it.hackcaffebabe.jdrive.local.watcher.events.Modify;
import it.hackcaffebabe.jdrive.local.watcher.events.Error;
import it.hackcaffebabe.jdrive.local.watcher.events.WatcherEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    /**
     * Instance a new UpLoader with a queue to take the events.
     * @param eventsQueue {@link java.util.concurrent.LinkedBlockingQueue} queue
     *        to take events from.
     * @throws Exception if something went wrong.
     */
    public UpLoader( LinkedBlockingQueue<WatcherEvent> eventsQueue ) throws Exception {
        this.eventsQueue = eventsQueue;
        this.driveFileManager = DriveFileManager.getInstance();
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
                if (detectedEvent instanceof Create) {
                    log.debug(((Create) detectedEvent).toString());

                    try {
                        File uploaded = driveFileManager.uploadFile(
                            detectedEvent.getFile()
                        );
                        DriveFileManager.logFile(uploaded);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                } else if (detectedEvent instanceof Modify) {
                    log.debug(((Modify) detectedEvent).toString());

                    try {
                        File updatedRemoteFile = driveFileManager.updateRemoteFile(
                            detectedEvent.getFile()
                        );
                        DriveFileManager.logFile(updatedRemoteFile);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                } else if (detectedEvent instanceof Delete) {
                    log.debug(((Delete) detectedEvent).toString());

                    try {
                        driveFileManager.deleteRemoteFileFrom( detectedEvent.getFile() );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                } else if (detectedEvent instanceof Error) {
                    log.debug(((Error) detectedEvent).toString());
                    keepRunning = false;
                }
            }

            log.info("Uploader closing.");
        } catch (Exception e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
