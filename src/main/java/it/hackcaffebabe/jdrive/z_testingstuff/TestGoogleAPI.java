package it.hackcaffebabe.jdrive.z_testingstuff;

import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.IOUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.Date;

/**
 * Testing Google API
 */
public class TestGoogleAPI {
    public static final Logger log = LogManager.getLogger("MyTest");

    public static void main(String[] args){
        try{
            PathsUtil.buildWorkingDirectory();
            Configurator.getInstance().load();

            Drive d = GoogleLoginWithGUI();
        }catch (IOException e){
            log.error(e.getMessage());
        }catch (InterruptedException ie){
            log.error(ie.getMessage());
        }
    }

    public static Drive GoogleLoginWithGUI() throws IOException, InterruptedException {
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

    public static void recList(Drive d, String parentID) throws IOException{
        log.info("retrieve file list");
        Drive.Files.List r = d.files().list();
        FileList fileList = r.setQ("'"+parentID+"' in parents and not trashed").execute();
        r.setPageToken(fileList.getNextPageToken());

        for(File f : fileList.getItems()) {
            log.info("====");
            log.info("Title: "+f.getTitle());
            log.info("ID: "+f.getId());
            log.info("MimeType: "+f.getMimeType());
            log.info("ParentID: "+f.getId());
            log.info("Parents:");
            for(int i=0; i<f.getParents().size(); i++){
                log.info(String.format("%d) %s", i, f.getParents().get(i).getId()));
            }
            if(f.getMimeType().endsWith("folder"))
                recList(d, f.getId());
        }
    }

    public static void recDownload(Drive d, String remoteID, String localID ) throws IOException {
        log.info("downloading files from "+ (localID==null?"root": localID) );
        Drive.Files.List r = d.files().list();
        FileList fileList = r.setQ("'"+remoteID+"' in parents and not trashed").execute();
        r.setPageToken(fileList.getNextPageToken());

        for( File f : fileList.getItems() ){
            // this is the local file
            java.io.File local;
            Path base = java.nio.file.Paths.get( (String)Configurator.getInstance().get(Keys.WATCHED_DIR) );
            if( localID == null ) { // means remoteID == root
                local = new java.io.File(base.toFile(), f.getTitle());
            }else {
                Path p = base.resolve(localID);
                local = new java.io.File(p.toFile(), f.getTitle());
            }

            InputStream is = downloadFile(d, f);
            if( is != null ){
                OutputStream out = new FileOutputStream(local);
                IOUtils.copy(is, out, true);
                out.close();
                log.info(String.format("file: %s downloaded.", f.getTitle()));
            } else {
                if( f.getMimeType().endsWith("folder") ){
                    log.info("The file is a folder");
                    if(!local.mkdirs())
                        throw new IOException("local.mkdirs() returns false.");
                    recDownload( d, f.getId(), f.getTitle() );
                }else{
                    log.info("File not recognized.");
                }
            }
        }
    }

    private static InputStream downloadFile(Drive service, File f) throws IOException{
        if (f.getDownloadUrl() != null && f.getDownloadUrl().length() > 0) {
            log.info(String.format("try to download %s...", f.getTitle()));
            HttpResponse resp = service.getRequestFactory().buildGetRequest(
                    new GenericUrl(f.getDownloadUrl())
            ).execute();
            return resp.getContent();
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }
}