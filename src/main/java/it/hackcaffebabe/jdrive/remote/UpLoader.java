package it.hackcaffebabe.jdrive.remote;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.Testing_API;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.fs.watcher.events.Create;
import it.hackcaffebabe.jdrive.fs.watcher.events.Delete;
import it.hackcaffebabe.jdrive.fs.watcher.events.Modify;
import it.hackcaffebabe.jdrive.fs.watcher.events.Error;
import it.hackcaffebabe.jdrive.fs.watcher.events.WatcherEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * TODO add doc
 */
public class UpLoader  implements Runnable
{
    private static final Logger log = LogManager.getLogger();
    private LinkedBlockingQueue<WatcherEvent> queueFromWatcher;
    private Drive driveService;
    private HashMap<Path, String> localToRemoteFileCombination = new HashMap<>();

    public UpLoader( LinkedBlockingQueue<WatcherEvent> queueFromWatcher ) throws GeneralSecurityException, IOException {
        this.queueFromWatcher = queueFromWatcher;
        this.driveService = GoogleAuthenticator.getInstance().getDriveService();
        log.info("Uploader ready to start.");
    }

    @Override
    public void run() {
        log.info("Uploaded started.");
        try {
            File JDriveRemoteFolder = getJDriveRemoteFolder();
            Testing_API.logFile( JDriveRemoteFolder );

            boolean keepRunning = true;
            WatcherEvent detectedEvent;
            while( keepRunning ){
                detectedEvent = queueFromWatcher.take();
                if( detectedEvent instanceof Create){
                    log.debug( ((Create)detectedEvent).toString() );
                    File uploaded = uploadLocalFile(
                            detectedEvent.getFile(),
                            JDriveRemoteFolder.getId()
                    );
                    Testing_API.logFile( uploaded );
                    localToRemoteFileCombination.put(
                            detectedEvent.getFile(),
                            uploaded.getId()
                    );
                }else if( detectedEvent instanceof Modify){
                    log.debug( ((Modify)detectedEvent).toString() );
                    String remoteFileId = localToRemoteFileCombination.get(
                            detectedEvent.getFile()
                    );
                    if( remoteFileId == null || remoteFileId.isEmpty() ){
                        throw new NoSuchElementException(
                            "No remote File associated with "+detectedEvent.getFile().toAbsolutePath()
                        );
                    }
                    List<File> files = listContentFrom( JDriveRemoteFolder );
                    File remoteFile = files.stream()
                            .filter(file -> file.getId().equals(remoteFileId))
                            .findAny()
                            .get();

                    File updatedRemoteFile = updateRemoteContent(
                            remoteFile, detectedEvent.getFile().toFile()
                    );
                    Testing_API.logFile(updatedRemoteFile);
                }else if( detectedEvent instanceof Delete){
                    log.debug( ((Delete)detectedEvent).toString() );
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

    private File getJDriveRemoteFolder() throws IOException {
        String qPattern = "mimeType = '%s' and not trashed and "+
                          "'root' in parents and name = 'JDrive'";
        String q = String.format( qPattern, Testing_API.MIME_TYPE_FOLDER );

        List<File> result = doQuery( q );

        if( result.isEmpty() ){
            throw new IOException( "JDrive remote folder not found." );
        }else if( result.size() > 1 ){
            throw new IOException( "Multiple JDrive remote folder found." );
        }

        return result.get(0);
    }

    private List<File> doQuery( String query ) throws IOException {
        Drive.Files.List request = driveService.files().list()
            .setQ( query )
            .setSpaces(Testing_API.DRIVE);

        List<File> result = new ArrayList<>();
        do {
            FileList files = request.execute();
            result.addAll(files.getFiles());

            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    private List<File> listContentFrom(File folder) throws IOException{
        String q = String.format("not trashed and '%s' in parents", folder.getId() );
        return doQuery( q );
    }

    private File uploadLocalFile(Path localFilePath, String parentId ) throws IOException {
        if( localFilePath == null )
            throw new IOException( "" );

        if( parentId == null || parentId.isEmpty() )
            throw new IOException( "" );

        java.io.File localFile = localFilePath.toFile();

        if( !localFile.exists() )
            throw new IOException( "File not exists: "+localFile.getAbsolutePath() );
        log.debug("Try to upload file: "+localFile.getAbsolutePath());

        File fileMetadata = new File()
            .setName( localFile.getName() )
            .setParents( Collections.singletonList(parentId) )
            .setDescription("description");

        InputStreamContent inputStreamContent = new InputStreamContent(
            null,
            new FileInputStream( localFile )
        );

        File uploaded = driveService.files()
                .create(fileMetadata, inputStreamContent)
                .execute();

        log.debug("upload ok.");
        return uploaded;
    }

    private File updateRemoteContent( File remoteFile, java.io.File updatedFile ) throws IOException{
        // This method doesn't work for docs, presentation spreadsheet ecc.
        // Maybe check if mime type of remoteFile to exclude them.
        log.info("Try to update file: "+updatedFile.getAbsolutePath());

        FileContent mediaContent = new FileContent( remoteFile.getMimeType(), updatedFile );
        File updatedRemoteFile = driveService.files()
            .update( remoteFile.getId(), new File(), mediaContent )
            .execute();

        log.info("update ok.");
        return updatedRemoteFile;
    }
}
