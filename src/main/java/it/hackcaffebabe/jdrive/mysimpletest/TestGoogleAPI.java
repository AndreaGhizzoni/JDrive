package it.hackcaffebabe.jdrive.mysimpletest;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.IOUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.Paths;
import it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Testing Google API
 */
public class TestGoogleAPI {
    public static final Logger log = LogManager.getLogger("MyTest");

    public static void main(String[] args){
        try{
            Paths.buildWorkingDirectory();
            Drive d = GoogleLoginWithGUI();
            downloadFilesFromRoot(d);
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    public static Drive GoogleLoginWithGUI() throws IOException{
        GoogleAuthenticator g = GoogleAuthenticator.getInstance();
        Drive d = g.UIAuthentication();
        log.info("Service get.");
        return d;
    }

    public static Drive GoogleLogin() throws IOException{
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
        return service;
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
        FileList fileList = r.setQ("'root' in parents and not trashed").execute();
        r.setPageToken(fileList.getNextPageToken());
        result.addAll(fileList.getItems());

        for(File f : result ) {
            log.info("====");
            log.info("Title: "+f.getTitle());
//            log.info("Created date: " + f.getCreatedDate());
//            log.info("Download url: "+f.getDownloadUrl());
//            log.info("====");
        }
    }

    public static void downloadFilesFromRoot( Drive d ) throws IOException{
        log.info("downloading files from root");
        Path base = java.nio.file.Paths.get("/home/andrea/Google Drive");

        List<File> result = new ArrayList<File>();
        Drive.Files.List r = d.files().list();
        FileList fileList = r.setQ("'root' in parents and not trashed").execute();
        r.setPageToken(fileList.getNextPageToken());
        result.addAll(fileList.getItems());

        for(File f : result ) {
            if( f.getDownloadUrl() != null && f.getDownloadUrl().length() > 0) {
                log.info(String.format("try to download %s...", f.getTitle()));
                HttpResponse resp = d.getRequestFactory().buildGetRequest(
                        new GenericUrl(f.getDownloadUrl())
                ).execute();
                InputStream is = resp.getContent();
                OutputStream out = new FileOutputStream(
                        new java.io.File(base.toFile(), f.getTitle())
                );

                IOUtils.copy(is, out, true);
                out.close();
                log.info(String.format("file: %s downloaded.", f.getTitle()));
            } else {
                log.info("The file doesn't have any content stored on Drive");
            }
        }
    }
}
