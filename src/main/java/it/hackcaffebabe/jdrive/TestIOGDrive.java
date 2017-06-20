package it.hackcaffebabe.jdrive;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


/**
 *
 */
public class TestIOGDrive {
    private static Logger log = LogManager.getLogger();

    public static void main(String... args){
        try{
            Path cfgPath = Paths.get(Constants.APP_PROPERTIES_FILE);
            Configurator.setup(cfgPath);
        }catch (Exception e){
            fatal(e.getMessage(), e);
        }

        Drive d = null;
        try{
            GoogleAuthenticator g = GoogleAuthenticator.getInstance();
            g.authenticate();
            d = g.getDriveService();
        }catch (Exception e){
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

        Path uploadPath = Paths.get("/home/andrea/Google Drive/upload");
        ArrayList<File> remoteFiles = new ArrayList<>();
        for( java.io.File f : uploadPath.toFile().listFiles() ){
            File remoteFile = uploadFile(d, f.toPath());
            remoteFiles.add(remoteFile);
        }

        for(File remoteFile : remoteFiles ){
            downloadRemoteFile(d,remoteFile);
        }
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

    private static void downloadRemoteFile(Drive driveService, File file){
        try {
            log.info("create output stream to local file...");
            OutputStream outputStream = new FileOutputStream(
                    Paths.get("/home/andrea/Google Drive/downloaded/"+file.getName())
                            .toFile()
            );
            log.info("try to download file: "+file.getName());
            driveService.files().get(file.getId())
                    .executeMediaAndDownloadTo(outputStream);
            log.info("download ok.");
        } catch (Exception e) {
            fatal(e.getMessage(), e);
        }
    }

    // from here https://goo.gl/zxWV9n
    private static File uploadFile(Drive service, Path localFilePath) {
        java.io.File localFile = localFilePath.toFile();

        // File's metadata.
        File body = new File()
                .setName(localFile.getName())
                .setDescription("description");
//                .setModifiedByMeTime(new DateTime(new Date(localFile.lastModified())));
        //body.setMimeType();

        // Set the parent folder.
//        if (parentId != null && parentId.length() > 0) {
//            body.setParents(
//                    Arrays.asList(new ParentReference().setId(parentId)));
//        }

        try {
            log.info("try to make input stream content...");
            InputStreamContent inputStreamContent = new InputStreamContent(
                    null,
                    new FileInputStream( localFile )
            );

            log.info("try to upload "+localFile.getAbsolutePath());
            return service.files().create(body, inputStreamContent)
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
