package it.hackcaffebabe.jdrive.testing;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.GoogleAuthenticator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestGoogleAPI {
    public static void main(String[] args){
        try {
            GoogleAuthenticator a = GoogleAuthenticator.getInstance();
            String url = a.getAuthURL();

            System.out.println("Please open the following URL in your browser then type the authorization code:");
            System.out.println("  " + url);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String code = br.readLine();

            Drive service = a.getService(code);

//            Insert a file
            File body = new File();
            body.setTitle("My document");
            body.setDescription("A test document");
            body.setMimeType("text/plain");

            java.io.File fileContent = new java.io.File("/home/andrea/document.txt");
            FileContent mediaContent = new FileContent("text/plain", fileContent);

            File file = service.files().insert(body, mediaContent).execute();
            System.out.println("File ID: " + file.getId());

            Drive anotherService = a.getService(null);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
