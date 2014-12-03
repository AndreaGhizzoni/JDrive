package it.hackcaffebabe.jdrive.testing;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.GoogleAuthenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Testing Google API
 */
public class TestGoogleAPI {
    public static final Logger log = LogManager.getLogger("TestGoogleAuthenticator");

    public static void main(String[] args){
        try{
            GoogleAuthenticator g = GoogleAuthenticator.getInstance();

            String code;
            String url = g.getAuthURL();
            if(url != null){
                log.info("Open the following url:");
                log.info(url);
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        System.in));
                code = br.readLine();
                g.setAuthResponseCode(code);
            }

            Drive service = g.getService();
            log.info("Service get.");


            listFilesInRoot(service);
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    public static void uploadSimpleFile(Drive d) throws IOException{
        File body = new File();
        body.setTitle("My Document-"+new Date());
        body.setDescription("A test document");
        body.setMimeType("text/plain");

        java.io.File fileContent = new java.io.File("test/document.txt");
        FileContent mediaContent = new FileContent("text/plain", fileContent);

        File file = d.files().insert(body, mediaContent).execute();
        log.info("File uploaded with ID:" +file.getId());
    }

    public static void listFilesInRoot( Drive d ) throws IOException{
        log.info("retrieve file list");
        List<File> result = new ArrayList<File>();
        Drive.Files.List r = d.files().list();
        FileList fileList = r.setQ("not trashed and ('root' in parents)").execute();
        result.addAll(fileList.getItems());

        for(File f : result ) {
            log.info(f.getTitle());
            log.info("====");
        }
    }
}
