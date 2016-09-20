package it.hackcaffebabe.jdrive;

import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.DateTime;
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
import java.util.Date;


/**
 *
 */
public class TestIOGDrive {
    private static Logger log = LogManager.getLogger();

    public static void main(String... args){
        try{
            Configurator.setup(new java.io.File(PathsUtil.APP_CGF_FILE));
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

        File insertedFile = uploadFile(d);
        log.info("id file: "+insertedFile.getId());
        downloadRemoteFile(d, insertedFile.getId());
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

    private static void downloadRemoteFile(Drive driveService, String fileId){
        try {
            log.info("create output stream to local file...");
            OutputStream outputStream = new FileOutputStream(
                    Paths.get("/home/andrea/Google Drive/downloaded/"+fileId).toFile()
            );
            log.info("try to download file: "+fileId);
            driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            log.info("download ok.");
        } catch (Exception e) {
            fatal(e.getMessage(), e);
        }
    }

    // from here https://goo.gl/zxWV9n
    private static File uploadFile(Drive service) {
        Path localFilePath = Paths.get("/home/andrea/Google Drive/test.txt");
        java.io.File localFile = localFilePath.toFile();

        // File's metadata.
        File body = new File()
                .setName("test.txt")
                .setDescription("description")
                .setModifiedByMeTime(new DateTime(new Date(localFile.lastModified())));
        //body.setMimeType();

        // Set the parent folder.
//        if (parentId != null && parentId.length() > 0) {
//            body.setParents(
//                    Arrays.asList(new ParentReference().setId(parentId)));
//        }

        try {
            log.info("Try to make input stream content...");
            InputStreamContent inputStreamContent = new InputStreamContent(
                    null,
                    new FileInputStream( localFile )
            );

            log.info("Try to upload...");
            return service.files().create(body, inputStreamContent).execute();
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
