package it.hackcaffebabe.jdrive;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class Testing_API {
    private static Logger log = LogManager.getLogger();
    private static Drive d;
    private static HashMap<String, String> MIMETypeConversion = new HashMap<>();

    public static void main(String... args) {
        try {
            Path cfgPath = Paths.get(Constants.APP_PROPERTIES_FILE);
            Configurator.setup(cfgPath);
        } catch (Exception e) {
            fatal(e.getMessage(), e);
        }

        try {
            GoogleAuthenticator g = GoogleAuthenticator.getInstance();
            g.authenticate();
            d = g.getDriveService();
        } catch (Exception e) {
            fatal(e.getMessage(), e);
        }

//        String id = createEmptyFile(d);
//        log.info("File ID: " + id);

//        Path localFilePath = Paths.get("/home/andrea/Google Drive/test.txt");
//        File insertedFile = uploadFile(d, localFilePath);
//        log.info("id file: "+insertedFile.getId());
//        downloadRemoteFile(d, insertedFile.getId());

        // FILEs TESTED:
        // binary     ok
        // zip, tar   ok
        // pdf        ok
        // plain text ok
        // jpg        ok

//        Path uploadPath = Paths.get("/home/andrea/Google Drive/upload");
//        ArrayList<File> remoteFiles = new ArrayList<>();
//        for( java.io.File f : uploadPath.toFile().listFiles() ){
//            File remoteFile = uploadFile(d, f.toPath());
//            remoteFiles.add(remoteFile);
//        }
//
//        for(File remoteFile : remoteFiles ){
//            downloadRemoteFile(d,remoteFile);
//        }


//        MIMETypeConversion.put(
//                "application/vnd.google-apps.spreadsheet", "application/pdf"
//        );
//        MIMETypeConversion.put(
//                "application/vnd.google-apps.document", "application/pdf"
//        );
//        MIMETypeConversion.put(
//                "application/vnd.google-apps.drawing", "image/png"
//        );
//        MIMETypeConversion.put(
//                "application/vnd.google-apps.presentation", "application/pdf"
//        );
//
        try {
            List<File> files = retrieveJDriveRemoteFolder(d);
            files.forEach(file -> logFile(file));
//
//            List<File> contents = retrieveFilesFrom(d, files.get(0));
//            contents.forEach(
//                file -> {
//                    logFile(file);
//                    downloadRemoteFile(d, file);
//                }
//            );

            Path fileToUpload = Paths.get(
                Configurator.getInstance().get(Keys.WATCHED_BASE_PATH)
                    + "/file1.txt"
            );
            uploadFile(d, fileToUpload, files.get(0).getId() );

        } catch (IOException e) {
            log.error(e.getMessage());
        }


    }

    private static List<File> retrieveJDriveRemoteFolder(Drive service) throws IOException {
        List<File> result = new ArrayList<>();
        String q = "mimeType = 'application/vnd.google-apps.folder' and " +
                   "not trashed and "+
                   "'root' in parents and "+
                   "name = 'JDrive'";
        Drive.Files.List request = service.files().list()
                .setQ( q )
                .setSpaces("drive");

        do {
            try {
                FileList files = request.execute();

                result.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                log.error("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                 request.getPageToken().length() > 0);

        return result;
    }

    private static List<File> retrieveFilesFrom(Drive service, File folder) throws IOException{
        List<File> result = new ArrayList<>();
        String q = "not trashed and "+
                   "'%s' in parents";
        Drive.Files.List request = service.files().list()
                .setQ( String.format(q, folder.getId()) )
                .setSpaces("drive");

        do {
            try {
                FileList files = request.execute();

                result.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                log.error("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    private static void downloadRemoteFile(Drive driveService, File file){
        try {
            String basePath = (String) Configurator.getInstance().get(Keys.WATCHED_BASE_PATH);
            java.io.File localFile = Paths.get(basePath+"/"+file.getName()).toFile();
            Files.createFile( localFile.toPath() );

            log.info("Try to download to: "+localFile.getAbsolutePath());
            OutputStream outputStream = new FileOutputStream(localFile);

            String conversion = MIMETypeConversion.getOrDefault(file.getMimeType(), "");
            log.info(file.getMimeType() +" -> "+ conversion);
            if( conversion.isEmpty() ){
                driveService.files().get(file.getId())
                    .executeMediaAndDownloadTo(outputStream);
            }else{
                driveService.files().export( file.getId(), conversion )
                    .executeMediaAndDownloadTo(outputStream);
            }

            log.info("download ok.");
        } catch (Exception e) {
            fatal(e.getMessage(), e);
        }
    }

    private static void logFile( File file ){
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

    // NB: invoke this method multiple times will always create a NEW file
    private static String createEmptyFile(Drive driveService){
        File fileMetadata = new File();
        fileMetadata.setName("Project plan.txt");
        fileMetadata.setMimeType("application/vnd.google-apps.drive-sdk");

        File file = null;
        try {
            log.info("Execution of remote file creation.");
            file = driveService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            fatal(e.getMessage(), e);
        }
        return file.getId();
    }

    // from here https://goo.gl/zxWV9n
    private static File uploadFile(Drive service, Path localFilePath,
                                   String parentId) {
        java.io.File localFile = localFilePath.toFile();

        try {
            File fileMetadata = new File()
                .setName(localFile.getName())
                .setParents( Collections.singletonList(parentId))
                .setDescription("description");

            log.info("try to make input stream content...");
            InputStreamContent inputStreamContent = new InputStreamContent(
                    null,
                    new FileInputStream( localFile )
            );

            log.info("try to upload "+localFile.getAbsolutePath());
            return service.files().create(fileMetadata, inputStreamContent)
                    .execute();
        } catch (IOException e) {
            fatal(e.getMessage(), e);
        }
        return null;
    }


    // this method write a fatal message into log file and kill the program
    private static void fatal(String msg, Throwable t){
        log.fatal(msg, t);
        System.exit(1);
    }
}
