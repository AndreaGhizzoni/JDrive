package it.hackcaffebabe.jdrive.testing;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.GoogleAuthenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestGoogleAPI {
    public static final Logger log = LogManager.getLogger("primary");

    public static void main(String[] args){
        try {
            GoogleAuthenticator a = GoogleAuthenticator.getInstance();
//            String url = a.getAuthURL();
//
//            log.info("Open the following url:");
//            log.info(url);
//            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//            String code = br.readLine();

            Drive service = a.getService(null);
            log.info("First instance of service created.");

//            a.store();

            File body = makeFile();
            java.io.File fileContent = new java.io.File("/home/andrea/document.txt");
            FileContent mediaContent = new FileContent("text/plain", fileContent);

            File file = service.files().insert(body, mediaContent).execute();
            log.info("File ID from first service: " + file.getId());

            Drive anotherService = a.getService(null);
            log.info("Second instance of service retrieved.");

            anotherService.files().insert(body, mediaContent).execute();
            log.info("File ID from second service: " + file.getId());
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static File makeFile(){
        File body = new File();
        body.setTitle("My document");
        body.setDescription("A test document");
        body.setMimeType("text/plain");
        return body;
    }
}
