package it.hackcaffebabe.jdrive;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Testing_API {
    private static Logger log = LogManager.getLogger();

    private static Drive driveService;

    public static final String DRIVE = "drive";

    private static final String MIME_TYPE_SPREADSHEET = "application/vnd.google-apps.spreadsheet";
    private static final String MIME_TYPE_DOCUMENT = "application/vnd.google-apps.document";
    private static final String MIME_TYPE_DRAWING = "application/vnd.google-apps.drawing";
    private static final String MIME_TYPE_PRESENTATION = "application/vnd.google-apps.presentation";
    public static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";
    private static HashMap<String, String> MIMETypeConversion = new HashMap<String, String>() {{
        put( MIME_TYPE_DOCUMENT, "application/pdf" );
        put( MIME_TYPE_SPREADSHEET, "application/pdf" );
        put( MIME_TYPE_DRAWING, "image/png" );
        put( MIME_TYPE_PRESENTATION, "application/pdf" );
    }};

    public static void main(String... args) {
        try {
            Configurator configurator = Configurator.setup(
                Paths.get( Constants.APP_PROPERTIES_FILE )
            );
            Path watcherBasePath = Paths.get(
                (String)configurator.get( Keys.WATCHED_BASE_PATH )
            );
            Files.createDirectories( watcherBasePath );
        } catch (Exception e) {
            fatal(e);
        }

        try {
            GoogleAuthenticator g = GoogleAuthenticator.getInstance();
            g.authenticate();
            driveService = g.getDriveService();
        } catch (Exception e) {
            fatal(e);
        }

        // FILEs TESTED:
        // binary     ok
        // zip, tar   ok
        // pdf        ok
        // plain text ok
        // jpg        ok

        // useful docs here: https://goo.gl/wbZZfs
        try {
            File jDriveRemoteFolder = getJDriveRemoteFolder();
            logFile( jDriveRemoteFolder );

            List<File> contents = listContentFrom( jDriveRemoteFolder );
            contents.forEach(
                file -> {
                    logFile(file);
                    try {
                        downloadRemoteFile(file);
//                        deleteRemoteFile(file);
                        trashRemoteFile(file);
                    } catch (IOException e) {
                        fatal(e);
                    }
                }
            );

            Path fileToUpload = createEmptyLocalFile( "file1.txt" );
            File remoteFile = uploadLocalFile( fileToUpload, jDriveRemoteFolder.getId() );
            log.info("Uploaded file with id: "+remoteFile.getId());

            writeTestLineInto( fileToUpload );
            File updatedFile = updateRemoteContent( remoteFile, fileToUpload.toFile() );
            log.info("Updated file with id: "+updatedFile.getId());
        } catch (IOException e) {
            fatal(e);
        }
    }

    private static File updateRemoteContent( File remoteFile, java.io.File updatedFile ) throws IOException{
        // This method doesn't work for docs, presentation spreadsheet ecc.
        // Maybe check if mime type of remoteFile to exclude them.
        log.info("Try to update file: "+updatedFile.getAbsolutePath());

        FileContent mediaContent = new FileContent( remoteFile.getMimeType(), updatedFile );
        File updatedRemoteFile = driveService.files()
            .update( remoteFile.getId(), new File(), mediaContent )
            .execute();

        log.info("Update ok.");
        return updatedRemoteFile;
    }

    private static void deleteRemoteFile( File file ) throws IOException {
        if( file == null )
            throw new IOException( "Given file can not be null." );
        log.info("Try to delete remote file: "+file.getName()+" with id: "+file.getId());

        driveService.files().delete( file.getId() ).execute();

        log.info("Delete ok:");
    }

    private static void trashRemoteFile( File file ) throws IOException {
        if( file == null )
            throw new IOException( "Given file can not be null." );
        log.info("Try to trash remote file: "+file.getName()+" with id: "+file.getId());

        File newContent = new File();
        newContent.setTrashed(true);
        driveService.files().update( file.getId(), newContent ).execute();

        log.info("Trash ok");
    }

    private static File getJDriveRemoteFolder() throws IOException {
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

    private static List<File> listContentFrom(File folder) throws IOException{
        String q = String.format("not trashed and '%s' in parents", folder.getId() );
        return doQuery( q );
    }

    private static List<File> doQuery( String query ) throws IOException {
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

    private static void downloadRemoteFile( File file ) throws IOException {
        if( file.getMimeType().equals(MIME_TYPE_FOLDER) ){
            log.info( "Try to Download a folder > SKIP." );
            return;
        }

        String basePath = (String) Configurator.getInstance().get(Keys.WATCHED_BASE_PATH);
        java.io.File localFile = Paths.get(
                basePath + PathsUtil.SEP + file.getName()
        ).toFile();

        log.info("Try to download to: "+localFile.getAbsolutePath());
        OutputStream outputStream = new FileOutputStream(localFile);

        String conversion = MIMETypeConversion.getOrDefault(file.getMimeType(), "");
        log.info(file.getMimeType() +" > converted to > "+ conversion);

        if( conversion.isEmpty() ){
            driveService.files().get( file.getId() )
                .executeMediaAndDownloadTo(outputStream);
        }else{
            driveService.files().export( file.getId(), conversion )
                .executeMediaAndDownloadTo(outputStream);
        }

        log.info("Download ok.");
    }

    private static File uploadLocalFile(Path localFilePath, String parentId ) throws IOException {
        if( localFilePath == null )
            throw new IOException( "" );

        if( parentId == null || parentId.isEmpty() )
            throw new IOException( "" );

        java.io.File localFile = localFilePath.toFile();

        if( !localFile.exists() )
            throw new IOException( "File not exists: "+localFile.getAbsolutePath() );
        log.info("Try to upload file: "+localFile.getAbsolutePath());

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

        log.info("upload ok.");
        return uploaded;
    }

    public static void logFile( File file ){
        log.info(">>>");
        log.info("File.getId(): "+file.getId());
        log.info("File.getName(): "+file.getName());
        log.info("File.getOriginalFileName(): "+file.getOriginalFilename());
        log.info("File.getDescription(): "+file.getDescription());
        log.info("File.getFileExtension(): "+file.getFileExtension());
        log.info("File.getFullFileExtension(): "+file.getFullFileExtension());
        log.info("File.getKind(): "+file.getKind());
        log.info("File.getMimeType(): "+file.getMimeType());
        log.info("File.getCreatedTime(): "+file.getCreatedTime());
        log.info("File.getLastModifyingUser(): "+file.getLastModifyingUser());
        log.info("File.getOwners(): "+file.getOwners());
        log.info("File.getParents(): "+file.getParents());
        log.info("File.getSize(): "+file.getSize());
        log.info("<<<");
    }

    private static Path createEmptyLocalFile( String name ) throws IOException{
        Path localEmptyFile = Paths.get( Configurator.getInstance()
                .get(Keys.WATCHED_BASE_PATH)+ PathsUtil.SEP + name );
        Files.createFile( localEmptyFile );
        return localEmptyFile;
    }

    private static void writeTestLineInto( Path localFile ) throws IOException {
        Files.write( localFile, Collections.singletonList("this is a line") );
    }

    private static void fatal(Exception e){
        log.fatal(e.getMessage());
        e.printStackTrace();
        System.exit(1);
    }
}
