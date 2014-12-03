package it.hackcaffebabe.jdrive.testing;

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


            listFiles(service);
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    public static void listFiles( Drive d ) throws IOException{
        log.info("retrieve file list");
        List<File> result = new ArrayList<File>();
        Drive.Files.List r = d.files().list();
        FileList fileList;
        do{
            fileList = r.setQ("not trashed").execute();
            result.addAll(fileList.getItems());

            r.setPageToken(fileList.getNextPageToken());
        }while( r.getPageToken() != null && r.getPageToken().length() > 0 );

        for(File f : result ) {
            log.info("Name: "+f.getTitle());
            log.info("EmbedLink: "+f.getEmbedLink());
            log.info("Kind: "+f.getKind());
            log.info("====");
        }
    }
}
