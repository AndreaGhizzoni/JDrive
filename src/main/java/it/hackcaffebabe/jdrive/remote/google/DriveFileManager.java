package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
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
    private RemoteToLocalFiles remoteToLocalFiles;

    private Path jdriveLocalBasePath = Paths.get(
        (String)Configurator.getInstance().get( Keys.WATCHED_BASE_PATH )
    );

    public static DriveFileManager getInstance() throws Exception {
        if( instance == null )
            instance = new DriveFileManager();
        return instance;
    }

    private DriveFileManager() throws Exception {
        driveService = GoogleAuthenticator.getInstance().getDriveService();
        remoteToLocalFiles = RemoteToLocalFiles.getInstance();

        File jdriveRemoteFolder = getJDriveRemoteFolderOrCreate();
        log.info("JDrive remote folder found.");

        remoteToLocalFiles.putAll( recursivelyListFrom( jdriveRemoteFolder.getId() ) );
        remoteToLocalFiles.put( jdriveRemoteFolder, jdriveLocalBasePath );
        log.info("Mapping remote and local file complete.");
    }

    private File createRemoteFolderFrom( Path localFolder ) throws IOException {
        if( localFolder.equals(jdriveLocalBasePath) ){
            return createRemoteFolder( jdriveLocalBasePath, "root" );
        }

        File remoteParentFile = remoteToLocalFiles.getIfExists( localFolder.getParent() );
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
            .setFields("id,modifiedTime,name,parents,trashed,mimeType")
            .execute();
        log.debug("Remote folder has been created with id="+remoteFolder.getId());
        remoteToLocalFiles.put( remoteFolder, folderPath );
        return remoteFolder;
    }

    public File uploadFile( Path localFilePath ) throws IOException {
        if( localFilePath == null )
            throw new IllegalArgumentException("Local file path can not be null");

        if ( localFilePath.toFile().isDirectory() )
            return createRemoteFolderFrom( localFilePath );

        File remoteParentFile = remoteToLocalFiles.getIfExists( localFilePath.getParent() );
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

        remoteToLocalFiles.put( fileUploaded, localFilePath );

        log.debug("Upload of "+localFile.getAbsolutePath()+" ok.");
        return fileUploaded;
    }

    public File updateRemoteFile( Path updatedFile ) throws IOException {
        if( updatedFile == null )
            throw new IllegalArgumentException("Updated file path can not be null");

        File remoteFile = remoteToLocalFiles.get( updatedFile );
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

        File remoteFile = remoteToLocalFiles.get( localFile );
        deleteRemoteFile( remoteFile );
    }

    private void deleteRemoteFile( File file ) throws IOException {
        log.info("Try to delete remote file with name="+file.getName() );
        driveService.files().delete( file.getId() ).execute();
        remoteToLocalFiles.remove( file );
        log.debug("Delete of remote file with name="+file.getName()+" ok");
    }

    public void trashRemoteFileFrom( Path localFile ) throws IOException {
        if( localFile == null )
            throw new IllegalArgumentException("Local file path to trash can not be null");
        if( !localFile.toFile().exists() )
            throw new IOException("Local file to trash does not exists");

        File remoteFile = remoteToLocalFiles.get( localFile );
        trashRemoteFile( remoteFile );
    }

    private void trashRemoteFile( File file ) throws IOException {
        log.info("Try to trash remote file with name="+file.getName());
        File newContent = new File().setTrashed(true);
        driveService.files().update( file.getId(), newContent ).execute();
        remoteToLocalFiles.remove( file );
        log.debug("Trash remote file with name="+file.getName()+" ok");
    }


    private File getJDriveRemoteFolderOrCreate() throws IOException {
        String queryPattern = "mimeType = '%s' and not trashed and "+
                              "'root' in parents and name = 'Google Drive'";
        String query = String.format( queryPattern, MIMEType.GOOGLE_FOLDER);

        List<File> result = doQuery( query );

        if( result.isEmpty() ){
            return createRemoteFolderFrom( jdriveLocalBasePath );
        }else if( result.size() > 1 ){
            throw new IOException( "Multiple JDrive remote folder found." );
        }

        return result.get(0);
    }

    private HashMap<File, Path> recursivelyListFrom( String remoteParentsId ) throws IOException {
        String q = String.format("not trashed and '%s' in parents", remoteParentsId );
        HashMap<File, Path> folderContent = new HashMap<>();
        doQuery( q ).forEach(
            file -> {
                logFile( file );
                folderContent.put( file, null );
                try {
                    if( file.getMimeType().equals(MIMEType.GOOGLE_FOLDER) ){
                        folderContent.putAll( recursivelyListFrom(file.getId()) );
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        );
        return folderContent;
    }

    private List<File> doQuery( String query ) throws IOException {
        Drive.Files.List request = driveService.files().list()
            .setQ( query )
            .setFields("files(id, name, parents, mimeType, kind, size, modifiedTime)")
            .setSpaces("drive");

        List<File> result = new ArrayList<>();
        do {
            FileList files = request.execute();
            result.addAll(files.getFiles());

            request.setPageToken(files.getNextPageToken());
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    public static void logFile( File file ) {
        String l = String.format(
            ">>> %s %s %s %s %s",
            file.getName(), file.getId(), file.getMimeType(),
            file.getModifiedTime(), file.getParents()
        );
        log.debug(l);
    }

    private class FileProgressListener
            implements MediaHttpUploaderProgressListener,
                       MediaHttpDownloaderProgressListener {
        @Override
        public void progressChanged(MediaHttpUploader uploader)
                throws IOException {

            if (uploader == null) return;
            switch (uploader.getUploadState()) {
                case INITIATION_STARTED:
                    log.debug("Upload initialization started");
                    break;
                case INITIATION_COMPLETE:
                    log.debug("Upload initialization complete");
                    break;
                case MEDIA_IN_PROGRESS:
                    log.debug("Upload: "+formatProgress(uploader));
                    break;
                case MEDIA_COMPLETE:
                    log.debug("Upload complete");
                    break;
            }
        }

        @Override
        public void progressChanged(MediaHttpDownloader downloader)
                throws IOException {

            if (downloader == null) return;
            switch (downloader.getDownloadState()){
                case NOT_STARTED:
                    log.debug("Download not stared");
                    break;
                case MEDIA_IN_PROGRESS:
                    log.debug("Download: "+formatProgress(downloader));
                    break;
                case MEDIA_COMPLETE:
                    log.debug("Download complete");
                    break;
            }
        }

        private String formatProgress( MediaHttpDownloader downloader ) throws IOException {
            return  String.valueOf(downloader.getNumBytesDownloaded()) +
                    " bytes | ~" +
                    formatPercentage(downloader.getProgress());
        }

        private String formatProgress( MediaHttpUploader uploader ) throws IOException {
            return  String.valueOf(uploader.getNumBytesUploaded()) +
                    " bytes | ~" +
                    formatPercentage(uploader.getProgress());
        }

        private String formatPercentage( double percentage ){
            return new DecimalFormat("#00.00").format( percentage*100 ) +" %";
        }
    }
}
