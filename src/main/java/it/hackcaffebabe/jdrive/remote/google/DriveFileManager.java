package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * TODO add doc
 */
public class DriveFileManager
{
    private static final Logger log = LogManager.getLogger();
    private static DriveFileManager instance;

    private Drive driveService;
    private File jDriveRemoteFolder;

    public static final String DRIVE = "drive";
    public static final String MIME_TYPE_SPREADSHEET = "application/vnd.google-apps.spreadsheet";
    public static final String MIME_TYPE_DOCUMENT = "application/vnd.google-apps.document";
    public static final String MIME_TYPE_DRAWING = "application/vnd.google-apps.drawing";
    public static final String MIME_TYPE_PRESENTATION = "application/vnd.google-apps.presentation";
    public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
    public static HashMap<String, String> MIMETypeConversion = new HashMap<String, String>() {{
        put( MIME_TYPE_DOCUMENT, "application/pdf" );
        put( MIME_TYPE_SPREADSHEET, "application/pdf" );
        put( MIME_TYPE_DRAWING, "image/png" );
        put( MIME_TYPE_PRESENTATION, "application/pdf" );
    }};

    public static DriveFileManager getInstance() throws Exception {
        if( instance == null )
            instance = new DriveFileManager();
        return instance;
    }

    private DriveFileManager() throws Exception {
        this.driveService = GoogleAuthenticator.getInstance().getDriveService();
        this.jDriveRemoteFolder = getJDriveRemoteFolder();
        log.info("JDrive remote folder found.");
    }

    public File uploadFile(Path localFilePath ) throws IOException {
        return uploadFile( localFilePath, jDriveRemoteFolder.getId() );
    }

    public File uploadFile(Path localFilePath, String remoteParentId ) throws IOException {
        if( localFilePath == null )
            throw new IllegalArgumentException( "Local file path can not be null" );

        if( remoteParentId == null || remoteParentId.isEmpty() )
            throw new IllegalArgumentException( "Remote parent id can not be null or empty" );

        java.io.File localFile = localFilePath.toFile();

        if( !localFile.exists() )
            throw new IOException( "File not exists: "+localFile.getAbsolutePath() );

        log.info("Try to upload file: "+localFile.getAbsolutePath());
        File fileMetadata = new File()
            .setName( localFile.getName() )
            .setParents( Collections.singletonList(remoteParentId) );

        InputStreamContent inputStreamContent = new InputStreamContent(
            null,
            new FileInputStream( localFile )
        );

        File fileUploaded = driveService.files()
            .create(fileMetadata, inputStreamContent)
            .execute();

        log.debug("Upload of "+localFile.getAbsolutePath()+" ok.");
        return fileUploaded;
    }

    public File updateRemoteContent( File remoteFile, java.io.File updatedFile ) throws IOException{
        if( remoteFile == null )
            throw new IllegalArgumentException("Remote file to update can not be null");

        if( updatedFile == null )
            throw new IllegalArgumentException("Updated local file can not be null");
        if( !updatedFile.exists() )
            throw new IOException("Updated local file doesn't exists");

        // This method doesn't work for docs, presentation spreadsheet ecc.
        // Maybe check if mime type of remoteFile to exclude them.
        log.info("Try to update remote copy of: "+updatedFile.getAbsolutePath());

        FileContent mediaContent = new FileContent( remoteFile.getMimeType(), updatedFile );
        File updatedRemoteFile = driveService.files()
            .update( remoteFile.getId(), new File(), mediaContent )
            .execute();

        log.debug("Update of remote file with id="+remoteFile.getId()+" ok.");
        return updatedRemoteFile;
    }

    public void deleteRemoteFile( File file ) throws IOException {
        deleteRemoteFile(file.getId());
    }

    public void deleteRemoteFile( String fileId ) throws IOException {
        if( fileId == null || fileId.isEmpty() )
            throw new IOException( "Given file id can not be null or empty." );

        log.info("Try to delete remote file with id="+fileId );
        driveService.files().delete( fileId ).execute();
        log.debug("Delete of remote file with id="+fileId+" ok");
    }

    public void trashRemoteFile( File file ) throws IOException {
        if( file == null )
            throw new IllegalArgumentException("remote file to trash can not be null");
        trashRemoteFile( file.getId() );
    }

    public void trashRemoteFile( String fileId ) throws IOException {
        if( fileId == null || fileId.isEmpty() )
            throw new IllegalArgumentException( "Given fileId can not be null or empty" );

        log.info("Try to trash remote file with id: "+fileId);
        File newContent = new File();
        newContent.setTrashed(true);
        driveService.files().update( fileId, newContent ).execute();
        log.debug("Trash remote file with id="+fileId+" ok");
    }

    private File getJDriveRemoteFolder() throws IOException {
        String queryPattern = "mimeType = '%s' and not trashed and "+
                              "'root' in parents and name = 'JDrive'";
        String query = String.format( queryPattern, MIME_TYPE_FOLDER );

        List<File> result = doQuery( query );

        if( result.isEmpty() ){
            throw new IOException( "JDrive remote folder not found." );
        }else if( result.size() > 1 ){
            throw new IOException( "Multiple JDrive remote folder found." );
        }

        return result.get(0);
    }

    public File getRemoteFileFromId( String remoteFileId ) throws IOException {
        if( remoteFileId == null || remoteFileId.isEmpty() )
            throw new IllegalArgumentException("remote file id can not be null or empty");

        log.info("Try to get remote file with id= "+remoteFileId);
        File remoteFile = getFolderContent( jDriveRemoteFolder ).stream()
            .filter(file -> file.getId().equals(remoteFileId))
            .findAny()
            .orElse(null);
        if( remoteFile == null ){
            throw new IOException("File with id="+remoteFileId+" not found");
        }
        return remoteFile;
    }

    private List<File> getFolderContent(File folder ) throws IOException{
        String q = String.format("not trashed and '%s' in parents", folder.getId() );
        return doQuery( q );
    }

    private List<File> doQuery( String query ) throws IOException {
        Drive.Files.List request = driveService.files().list()
            .setQ( query )
            .setSpaces(DRIVE);

        List<File> result = new ArrayList<>();
        do {
            FileList files = request.execute();
            result.addAll(files.getFiles());

            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }
}
