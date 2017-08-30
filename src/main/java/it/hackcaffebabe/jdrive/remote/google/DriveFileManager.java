package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * TODO add doc
 * PROBLEMS:
 * - rename a folder with files in it: folder will be rename but is empty on JDrive
 */
public class DriveFileManager
{
    private static final Logger log = LogManager.getLogger();
    private static DriveFileManager instance;

    private Drive driveService;
    private File jDriveRemoteFolder;

    private HashMap<File, Path> remoteToLocalFiles;

    private Path jdriveBasePath = Paths.get(
        (String)Configurator.getInstance().get( Keys.WATCHED_BASE_PATH )
    );

    public static DriveFileManager getInstance() throws Exception {
        if( instance == null )
            instance = new DriveFileManager();
        return instance;
    }

    private DriveFileManager() throws Exception {
        this.driveService = GoogleAuthenticator.getInstance().getDriveService();

        this.jDriveRemoteFolder = getJDriveRemoteFolder();
        log.info("JDrive remote folder found.");
        this.remoteToLocalFiles = recursivelyListFrom( this.jDriveRemoteFolder.getId() );
        this.addToMap(jDriveRemoteFolder, jdriveBasePath);
        log.info("Mapping remote and local file complete.");
    }

    private File createRemoteFolderFrom( Path localFolder ) throws IOException {
        Path parent = localFolder.getParent();
        log.debug("parent is "+parent);

        if( parent.equals(jdriveBasePath) ){
            return createRemoteFolder( localFolder, jDriveRemoteFolder );
        }else{
            File remoteParentFile = getRemoteFileFromLocalPathIfExists( parent );
            if( remoteParentFile != null ){
                return createRemoteFolder( localFolder, remoteParentFile );
            }else {
                return createRemoteFolderFrom( parent );
            }
        }
    }

    private File createRemoteFolder( Path folderPath, File parentRemoteFile ) throws IOException {
        // TODO do some checks
        log.info("Try to create remote folder from "+folderPath);
        File fileMetadata = new File();
        fileMetadata.setName( folderPath.getFileName().toString() );
        fileMetadata.setMimeType( MIMEType.FOLDER );
        fileMetadata.setParents( Collections.singletonList(parentRemoteFile.getId()) );
        File remoteFolder = driveService.files()
                .create( fileMetadata )
                .setFields("id,modifiedTime,name,parents,trashed,mimeType")
                .execute();
        log.debug("Remote folder has been created with id="+remoteFolder.getId());
        this.addToMap( remoteFolder, folderPath );
        return remoteFolder;
    }

    public File uploadFile( Path localFilePath ) throws IOException {
        if( localFilePath == null )
            throw new IllegalArgumentException("Local file path can not be null");

        if ( localFilePath.toFile().isDirectory() )
            return createRemoteFolderFrom( localFilePath );

        Path parent = localFilePath.getParent();
        File remoteParentFile = getRemoteFileFromLocalPathIfExists( parent );
        if( remoteParentFile == null ) {
            remoteParentFile = createRemoteFolderFrom(parent);
        }

        return uploadFile(localFilePath, remoteParentFile.getId());
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

        String mimeType;
        if( localFile.isDirectory() ){
            mimeType = MIMEType.FOLDER;
        }else{
            mimeType = MIMEType.Conversion.get(
                PathsUtil.getFileExtension(localFile)
            );
        }

        File fileUploaded = driveService.files()
            .create(fileMetadata, new FileContent(mimeType, localFile) )
            .setFields("id,modifiedTime,name,parents,trashed,mimeType")
            .execute();

        this.addToMap( fileUploaded, localFilePath );
        log.debug("Upload of "+localFile.getAbsolutePath()+" ok.");
        return fileUploaded;
    }

    public File updateRemoteFile( Path updatedFile ) throws IOException {
        if( updatedFile == null )
            throw new IllegalArgumentException("Updated file path can not be null");

        File remoteFile = this.getRemoteFileFromLocalPath( updatedFile );
        return updateRemoteFile( remoteFile, updatedFile.toFile() );
    }

    private File updateRemoteFile( File remoteFile, java.io.File updatedFile ) throws IOException {
        if( remoteFile == null )
            throw new IllegalArgumentException("Remote file to update can not be null");

        if( updatedFile == null )
            throw new IllegalArgumentException("Updated local file can not be null");
        if( !updatedFile.exists() )
            throw new IOException("Updated local file doesn't exists");

        // This method doesn't work for docs, presentation spreadsheet ecc.
        // Maybe check if mime type of remoteFile to exclude them.
        log.info("Try to update remote copy of: "+updatedFile.getAbsolutePath());

        File fileMetadata = new File();
        fileMetadata.setName( updatedFile.getName() );

        FileContent mediaContent;
        File updatedRemoteFile;
        if( updatedFile.isDirectory() ){
            updatedRemoteFile = driveService.files()
                .update( remoteFile.getId(), fileMetadata )
                .setFields("id,modifiedTime,name,parents,trashed,mimeType")
                .execute();
        }else{
            String mimeType = MIMEType.Conversion.get(
                PathsUtil.getFileExtension(updatedFile)
            );
            mediaContent = new FileContent( mimeType, updatedFile );
            updatedRemoteFile = driveService.files()
                .update( remoteFile.getId(), fileMetadata, mediaContent )
                .setFields("id,modifiedTime,name,parents,trashed,mimeType")
                .execute();
        }

        log.debug("Update of remote file with id="+remoteFile.getId()+" ok.");
        return updatedRemoteFile;
    }

    public void deleteRemoteFileFrom( Path localFile ) throws IOException {
        if( localFile == null )
            throw new IllegalArgumentException("Local file path to delete can not be null");

        File remoteFile = this.getRemoteFileFromLocalPath( localFile );
        deleteRemoteFile( remoteFile );
    }

    private void deleteRemoteFile( File file ) throws IOException {
        if( file == null )
            throw new IllegalArgumentException("Remote file to delete can not be null");

        log.info("Try to delete remote file with name="+file.getName() );
        driveService.files().delete( file.getId() ).execute();
        this.deleteFromMap( file );
        log.debug("Delete of remote file with name="+file.getName()+" ok");
    }

    public void trashRemoteFileFrom( Path localFile ) throws IOException {
        if( localFile == null )
            throw new IllegalArgumentException("Local file path to trash can not be null");
        if( !localFile.toFile().exists() )
            throw new IOException("Local file to trash does not exists");

        File remoteFile = this.getRemoteFileFromLocalPath( localFile );
        trashRemoteFile( remoteFile );
    }

    private void trashRemoteFile( File file ) throws IOException {
        if( file == null )
            throw new IllegalArgumentException("remote file to trash can not be null");

        log.info("Try to trash remote file with name="+file.getName());
        File newContent = new File();
        newContent.setTrashed(true);
        driveService.files().update( file.getId(), newContent ).execute();
        this.deleteFromMap( file );
        log.debug("Trash remote file with name="+file.getName()+" ok");
    }


    private File getJDriveRemoteFolder() throws IOException {
        String queryPattern = "mimeType = '%s' and not trashed and "+
                              "'root' in parents and name = 'JDrive'";
        String query = String.format( queryPattern, MIMEType.FOLDER );

        List<File> result = doQuery( query );

        if( result.isEmpty() ){
            throw new IOException( "JDrive remote folder not found." );
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
                    if( file.getMimeType().equals(MIMEType.FOLDER) ){
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

    private void addToMap( File remoteFile, Path localFilePath ) {
        this.remoteToLocalFiles.put( remoteFile, localFilePath );
    }

    private void deleteFromMap( File remoteFile ) {
        this.remoteToLocalFiles.remove( remoteFile );
    }

    private File getRemoteFileFromLocalPath( Path localFilePath ) throws IOException {
        File file = getRemoteFileFromLocalPathIfExists( localFilePath );
        if( file == null )
            throw new IOException(
                    "Remote file for local path "+localFilePath+" not found");
        return file;
    }

    private File getRemoteFileFromLocalPathIfExists( Path localFilePath ) {
        Map.Entry<File, Path> mapEntry = this.remoteToLocalFiles.entrySet()
            .stream()
            .filter( entry -> entry.getValue() != null && entry.getValue().toAbsolutePath().equals(localFilePath) )
            .findAny()
            .orElse(null);

        return mapEntry == null ? null : mapEntry.getKey();
    }
}
