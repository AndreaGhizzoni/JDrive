package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.mapping.MappedFileSystem;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * TODO add doc
 * PROBLEMS:
 * - rename a folder with files in it: folder will be rename but is empty on JDrive
 * ==> possible bug: https://bugs.openjdk.java.net/browse/JDK-8162948
 *
 * Download and upload speed counter obtained form: https://goo.gl/wtKHvT
 */
public class DriveFileManager
{
    private static final Logger log = LogManager.getLogger();
    private static DriveFileManager instance;

    private Drive driveService;
    private MappedFileSystem mappedFileSystem;

    private Path jdriveLocalBasePath = Paths.get(
        (String)Configurator.getInstance().get( Keys.WATCHED_BASE_PATH )
    );

    public static DriveFileManager getInstance() throws GeneralSecurityException, IOException {
        if( instance == null )
            instance = new DriveFileManager();
        return instance;
    }

    private DriveFileManager() throws GeneralSecurityException, IOException {
        driveService = GoogleAuthenticator.getInstance().getDriveService();
        mappedFileSystem = MappedFileSystem.getInstance();
    }

    public File createRemoteFolderFrom( Path localFolder ) throws IOException {
        if( localFolder.equals(jdriveLocalBasePath) ){
            return createRemoteFolder( jdriveLocalBasePath, "root" );
        }

        File remoteParentFile = mappedFileSystem.get( localFolder.getParent() );
        if( remoteParentFile != null ){
            return createRemoteFolder( localFolder, remoteParentFile.getId() );
        }else {
            return createRemoteFolderFrom( localFolder.getParent() );
        }
    }

    private File createRemoteFolder( Path folderPath, String parentRemoteFileId ) throws IOException {
        log.info("Try to create remote folder from "+folderPath);
        File fileMetadata = new File();
        fileMetadata.setName( folderPath.getFileName().toString() );
        fileMetadata.setMimeType( MIMEType.GOOGLE_FOLDER);
        fileMetadata.setParents( Collections.singletonList(parentRemoteFileId) );
        File remoteFolder = driveService.files()
            .create( fileMetadata )
            .setFields("id,modifiedTime,name,parents,trashed,mimeType,size,kind")
            .execute();
        log.debug("Remote folder has been created with id="+remoteFolder.getId());
        mappedFileSystem.put( folderPath, remoteFolder );
        return remoteFolder;
    }

    public File uploadFile( Path localFilePath ) throws IOException {
        if( localFilePath == null )
            throw new IllegalArgumentException("Local file path can not be null");

        if ( localFilePath.toFile().isDirectory() )
            return createRemoteFolderFrom( localFilePath );

        File remoteParentFile = mappedFileSystem.get( localFilePath.getParent() );
        if( remoteParentFile == null ) {
            remoteParentFile = createRemoteFolderFrom( localFilePath.getParent() );
        }

        return uploadFile( localFilePath, remoteParentFile.getId() );
    }

    private File uploadFile( Path localFilePath, String remoteParentId ) throws IOException {
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

        String mimeType = MIMEType.convert(
            PathsUtil.getFileExtension(localFile)
        );

        Drive.Files.Create create = driveService.files()
            .create(fileMetadata, new FileContent(mimeType, localFile) );

        create.getMediaHttpUploader()
            .setDirectUploadEnabled( false )
            .setChunkSize( MediaHttpUploader.MINIMUM_CHUNK_SIZE )
            .setProgressListener( new FileProgressListener() );

        File fileUploaded = create
            .setFields("id,modifiedTime,name,parents,trashed,mimeType")
            .execute();

        mappedFileSystem.put( localFilePath, fileUploaded );

        log.debug("Upload of "+localFile.getAbsolutePath()+" ok.");
        return fileUploaded;
    }

    public File updateRemoteFile( Path updatedFile ) throws IOException {
        if( updatedFile == null )
            throw new IllegalArgumentException("Updated file path can not be null");

        File remoteFile = mappedFileSystem.get( updatedFile );
        return updateRemoteFile( remoteFile, updatedFile.toFile() );
    }

    private File updateRemoteFile( File remoteFile, java.io.File updatedFile ) throws IOException {
        if( updatedFile == null )
            throw new IllegalArgumentException("Updated local file can not be null");
        if( !updatedFile.exists() )
            throw new IOException("Updated local file doesn't exists");

        // This method doesn't work for docs, presentation spreadsheet ecc.
        // Maybe check if mime type of remoteFile to exclude them.
        log.info("Try to update remote copy of: "+updatedFile.getAbsolutePath());

        File fileMetadata = new File().setName( updatedFile.getName() );

        Drive.Files.Update update;
        if( updatedFile.isDirectory() ){
            update = driveService.files().update( remoteFile.getId(), fileMetadata );
        }else{
            String mimeType = MIMEType.convert(
                PathsUtil.getFileExtension(updatedFile)
            );
            FileContent mediaContent = new FileContent( mimeType, updatedFile );
            update = driveService.files()
                    .update( remoteFile.getId(), fileMetadata, mediaContent );

            update.getMediaHttpUploader()
                .setDirectUploadEnabled( false )
                .setChunkSize( MediaHttpUploader.MINIMUM_CHUNK_SIZE )
                .setProgressListener( new FileProgressListener() );
        }

        File updatedRemoteFile = update
            .setFields("id,modifiedTime,name,parents,trashed,mimeType")
            .execute();

        log.debug("Update of remote file with id="+remoteFile.getId()+" ok.");
        return updatedRemoteFile;
    }

    public void deleteRemoteFileFrom( Path localFile ) throws IOException {
        if( localFile == null )
            throw new IllegalArgumentException("Local file path to delete can not be null");

        File remoteFile = mappedFileSystem.get( localFile );
        deleteRemoteFile( remoteFile );
    }

    private void deleteRemoteFile( File file ) throws IOException {
        log.info("Try to delete remote file with name="+file.getName() );
        driveService.files().delete( file.getId() ).execute();
        mappedFileSystem.remove( file );
        log.debug("Delete of remote file with name="+file.getName()+" ok");
    }

    public void trashRemoteFileFrom( Path localFile ) throws IOException {
        if( localFile == null )
            throw new IllegalArgumentException("Local file path to trash can not be null");
        if( !localFile.toFile().exists() )
            throw new IOException("Local file to trash does not exists");

        File remoteFile = mappedFileSystem.get( localFile );
        trashRemoteFile( remoteFile );
    }

    private void trashRemoteFile( File file ) throws IOException {
        log.info("Try to trash remote file with name="+file.getName());
        File newContent = new File().setTrashed(true);
        driveService.files().update( file.getId(), newContent ).execute();
        mappedFileSystem.remove( file );
        log.debug("Trash remote file with name="+file.getName()+" ok");
    }

    public static void logFile( File file ) {
        String l = String.format(
            ">>> %s %s %s %s %s",
            file.getName(), file.getId(), file.getMimeType(),
            file.getModifiedTime(), file.getParents()
        );
        log.debug(l);
    }
}
