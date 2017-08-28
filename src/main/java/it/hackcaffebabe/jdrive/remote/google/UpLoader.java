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

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TODO add doc
 */
public class UpLoader  implements Runnable
{
    private static final Logger log = LogManager.getLogger();
    private LinkedBlockingQueue<WatcherEvent> queueFromWatcher;
    private DriveFileManager driveFileManager;
    private HashMap<Path, String> localToRemoteFileCombination = new HashMap<>();

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
            while( keepRunning ){
                detectedEvent = queueFromWatcher.take();
                if( detectedEvent instanceof Create ){
                    log.debug( ((Create)detectedEvent).toString() );
                    File uploaded = driveFileManager.uploadFile(
                        detectedEvent.getFile()
                    );
                    Testing_API.logFile( uploaded );
                    localToRemoteFileCombination.put(
                        detectedEvent.getFile(),
                        uploaded.getId()
                    );
                }else if( detectedEvent instanceof Modify ){
                    log.debug( ((Modify)detectedEvent).toString() );
                    String remoteFileId = localToRemoteFileCombination.get(
                        detectedEvent.getFile()
                    );
                    if( remoteFileId == null || remoteFileId.isEmpty() ){
                        throw new NoSuchElementException(
                            "No remote File associated with "+detectedEvent.getFile().toAbsolutePath()
                        );
                    }

                    File remoteFile = driveFileManager.getRemoteFileFromId(
                        remoteFileId
                    );
                    File updatedRemoteFile = driveFileManager.updateRemoteContent(
                            remoteFile, detectedEvent.getFile().toFile()
                    );
                    Testing_API.logFile(updatedRemoteFile);
                }else if( detectedEvent instanceof Delete ){
                    log.debug( ((Delete)detectedEvent).toString() );
                    String remoteFileId = localToRemoteFileCombination.get(
                        detectedEvent.getFile()
                    );
                    if( remoteFileId == null || remoteFileId.isEmpty() ){
                        throw new NoSuchElementException(
                            "No remote File associated with "+detectedEvent.getFile().toAbsolutePath()
                        );
                    }

                    driveFileManager.deleteRemoteFile( remoteFileId );
                }else if( detectedEvent instanceof Error ){
                    log.debug( ((Error)detectedEvent).toString() );
                    keepRunning = false;
                }
            }
            log.info("Uploader closing.");

        } catch (Exception e) {
            log.fatal(e.getMessage(), e);
        }
    }
}
