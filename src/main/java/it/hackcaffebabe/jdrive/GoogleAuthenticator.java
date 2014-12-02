package it.hackcaffebabe.jdrive;

import static it.hackcaffebabe.jdrive.UtilConst.*;
import static it.hackcaffebabe.jdrive.AuthConst.*;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * https://stackoverflow.com/questions/20684238/authorization-to-google-client
 * https://stackoverflow.com/questions/19861178/stored-credential-from-google-api-to-be-reused-java
 */
public final class GoogleAuthenticator
{
    private static GoogleAuthenticator instance;
    private static final Logger log = LogManager.getLogger("primary");

    public static GoogleAuthenticator getInstance() throws IOException {
        if (instance == null)
            instance = new GoogleAuthenticator();
        return instance;
    }

    private HttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private GoogleAuthorizationCodeFlow googleAuthCodeFlow;
    private DataStore<StoredCredential> store;

    private GoogleAuthenticator() throws IOException {
        this.buildHTTPTransportJsonFactory();
        this.buildDataStore();
        this.buildGoogleAuthCodeFlow();
    }

//==============================================================================
// METHOD
//==============================================================================
    private void buildHTTPTransportJsonFactory() {
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = new JacksonFactory();
    }

    private void buildGoogleAuthCodeFlow(){
        this.googleAuthCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                CLIENT_ID,
                CLIENT_SECRET,
                Arrays.asList(DriveScopes.DRIVE)
        ).setAccessType("offline").setApprovalPrompt("force").build();
    }

    private void buildDataStore() throws IOException{
        this.store = new MemoryDataStoreFactory().getDataStore(STORE_NAME);

        File f = new File("test.json");
        if(f.exists())
            load();
    }

    public void store(){
        try{
            com.fasterxml.jackson.core.JsonGenerator j = new
                    com.fasterxml.jackson.core.JsonFactory().createGenerator(
                    new File("test.json"), JsonEncoding.UTF8 );

            StoredCredential c = this.store.get(TOKEN_NAME);
            j.writeStartObject();// {

            j.writeStringField("access_token", c.getAccessToken());
            j.writeStringField("refresh_token", c.getRefreshToken());

            j.writeEndObject();// }
            j.flush();
            j.close();
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }

    public void load(){
       try{
           JsonParser p = new com.fasterxml.jackson.core.JsonFactory()
                   .createJsonParser(new File("test.json"));

           StoredCredential s = new StoredCredential(makeGoogleCredential());

           String fieldName;
           while (p.nextToken() != JsonToken.END_OBJECT) {
               fieldName = p.getCurrentName();
               if("access_token".equals(fieldName)){
                   p.nextToken();
                   s.setAccessToken(p.getText());
                   log.debug("access_token: "+s.getAccessToken());
               }

               if("refresh_token".equals(fieldName)){
                   p.nextToken();
                   s.setRefreshToken(p.getText());
                   log.debug("refresh_token: "+s.getRefreshToken());
               }
           }
           p.close();

           this.store.set(TOKEN_NAME, s);
       }catch (IOException e){
           log.error(e.getMessage());
       }
    }

    private GoogleCredential makeGoogleCredential(){
        return new GoogleCredential.Builder()
                .setTransport(this.httpTransport)
                .setJsonFactory(this.jsonFactory)
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
//                .addRefreshListener(new CredentialRefreshListener() {
//                    @Override
//                    public void onTokenResponse(Credential credential,
//                                                TokenResponse tokenResponse) throws IOException {
//                        System.out.println("successfully");
//                    }
//                    @Override
//                    public void onTokenErrorResponse(Credential credential,
//                                                     TokenErrorResponse tokenErrorResponse) throws IOException {
//                        System.out.println("wrong");
//                    }
//                })
                .build();
    }

//==============================================================================
// GETTER
//==============================================================================
    public String getAuthURL() {
        return this.googleAuthCodeFlow.newAuthorizationUrl().
                setRedirectUri(REDIRECT_URI).build();
    }

    public Drive getService(String auth) throws IOException {
        GoogleCredential cred = makeGoogleCredential();

        if(this.store.containsKey(TOKEN_NAME)){
            log.debug("Token present into the store.");
            StoredCredential sc = this.store.get(TOKEN_NAME);
            cred.setAccessToken(sc.getAccessToken());
            cred.setRefreshToken(sc.getRefreshToken());
        }else{
            log.debug("Token not present into the store.");
            GoogleTokenResponse t = this.googleAuthCodeFlow.newTokenRequest(auth)
                .setRedirectUri(REDIRECT_URI).execute();
            cred.setFromTokenResponse(t);
            cred.setAccessToken(ACCESS_TOKEN);
            this.store.set(TOKEN_NAME, new StoredCredential(cred));
            log.debug("Token stored.");
        }

        return new Drive.Builder(this.httpTransport, this.jsonFactory, cred)
                .setApplicationName(APP_NAME).build();
    }
}
