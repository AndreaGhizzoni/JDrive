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

    /**
     * TODO add doc
     * @param remoteFileId
     * @return
     * @throws IOException
     */
    public File getRemoteFileFromId( String remoteFileId ) throws IOException {
        List<File> files = listContentFrom( jDriveRemoteFolder );
        File remoteFile = files.stream()
            .filter(file -> file.getId().equals(remoteFileId))
            .findAny()
            .orElse(null);
        if( remoteFile == null ){
            throw new IOException("File with id "+remoteFileId+" not found");
        }
        return remoteFile;
    }

    /**
     * TODO add doc
     * @param localFilePath
     * @return
     * @throws IOException
     */
    public File uploadLocalFile( Path localFilePath ) throws IOException {
        return uploadLocalFile( localFilePath, jDriveRemoteFolder.getId() );
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
            .setParents( Collections.singletonList(parentId) );
//            .setDescription("description");

        InputStreamContent inputStreamContent = new InputStreamContent(
            null,
            new FileInputStream( localFile )
        );

        File fileUploaded = driveService.files()
            .create(fileMetadata, inputStreamContent)
            .execute();

        log.debug("upload ok.");
        return fileUploaded;
    }

    /**
     * TODO add doc
     * @param remoteFile
     * @param updatedFile
     * @return
     * @throws IOException
     */
    public File updateRemoteContent( File remoteFile, java.io.File updatedFile ) throws IOException{
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

    private File getJDriveRemoteFolder() throws IOException {
        String qPattern = "mimeType = '%s' and not trashed and "+
                          "'root' in parents and name = 'JDrive'";
        String q = String.format( qPattern, MIME_TYPE_FOLDER );

        List<File> result = doQuery( q );

        if( result.isEmpty() ){
            throw new IOException( "JDrive remote folder not found." );
        }else if( result.size() > 1 ){
            throw new IOException( "Multiple JDrive remote folder found." );
        }

        return result.get(0);
    }

    private List<File> listContentFrom(File folder) throws IOException{
        String q = String.format("not trashed and '%s' in parents", folder.getId() );
        return doQuery( q );
    }
}
