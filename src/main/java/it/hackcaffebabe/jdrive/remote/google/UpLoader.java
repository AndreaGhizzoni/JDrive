package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.Testing_API;
import it.hackcaffebabe.jdrive.fs.watcher.events.Create;
import it.hackcaffebabe.jdrive.fs.watcher.events.Delete;
import it.hackcaffebabe.jdrive.fs.watcher.events.Modify;
import it.hackcaffebabe.jdrive.fs.watcher.events.Error;
import it.hackcaffebabe.jdrive.fs.watcher.events.WatcherEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TODO add doc
 */
public class UpLoader  implements Runnable
{
    private static final Logger log = LogManager.getLogger();
    private LinkedBlockingQueue<WatcherEvent> queueFromWatcher;
    private DriveFileManager driveFileManager;

    public UpLoader( LinkedBlockingQueue<WatcherEvent> queueFromWatcher ) throws Exception {
        this.queueFromWatcher = queueFromWatcher;
        this.driveFileManager = DriveFileManager.getInstance();
        log.info("Uploader ready to start.");
    }

    @Override
    public void run() {
        log.info("Uploaded started.");

        try {
            boolean keepRunning = true;
            WatcherEvent detectedEvent;
            while( keepRunning ) {
                detectedEvent = queueFromWatcher.take();
                if (detectedEvent instanceof Create) {
                    log.debug(((Create) detectedEvent).toString());

                    try {
                        File uploaded = driveFileManager.uploadFile(
                            detectedEvent.getFile()
                        );
                        Testing_API.logFile(uploaded);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                } else if (detectedEvent instanceof Modify) {
                    log.debug(((Modify) detectedEvent).toString());

                    try {
                        File updatedRemoteFile = driveFileManager.updateRemoteFile(
                            detectedEvent.getFile()
                        );
                        Testing_API.logFile(updatedRemoteFile);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                } else if (detectedEvent instanceof Delete) {
                    log.debug(((Delete) detectedEvent).toString());

                    try {
                        driveFileManager.deleteRemoteFileFrom( detectedEvent.getFile() );
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                } else if (detectedEvent instanceof Error) {
                    log.debug(((Error) detectedEvent).toString());
                    keepRunning = false;
                }
            }

            log.info("Uploader closing.");
        } catch (InterruptedException e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
