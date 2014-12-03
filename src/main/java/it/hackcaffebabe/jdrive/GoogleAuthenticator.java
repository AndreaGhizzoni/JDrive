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
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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

    private GoogleAuthenticator.Status status;
    private HttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private GoogleAuthorizationCodeFlow googleAuthCodeFlow;
    private GoogleTokenResponse tokenResponse;
    private DataStore<StoredCredential> store;
    private Drive service;

    private GoogleAuthenticator() throws IOException {
        this.status = Status.UNAUTHORIZED;
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

    private void buildDataStore() throws IOException{
        this.store = new MemoryDataStoreFactory().getDataStore(STORE_NAME);
        loadCredential();
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

    private void storeCredential(){
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

    private void loadCredential(){
        File f = new File("test.json");
        if(!f.exists())
            return;

        try{
            log.info("Credential found: try to load.");
            JsonParser p = new com.fasterxml.jackson.core.JsonFactory()
                       .createJsonParser(f);

            StoredCredential s = new StoredCredential(makeGoogleCredential());

            String fieldName;
            while (p.nextToken() != JsonToken.END_OBJECT) {
                fieldName = p.getCurrentName();
                    if("access_token".equals(fieldName)){
                        p.nextToken();
                        s.setAccessToken(p.getText());
                    }

                    if("refresh_token".equals(fieldName)){
                        p.nextToken();
                        s.setRefreshToken(p.getText());
                    }
                }
                p.close();

            this.store.set(TOKEN_NAME, s);
            this.status = Status.AUTHORIZE;
            log.info("Credential loaded.");
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

    public void setAuthResponseCode(String code) throws IOException{
        if(this.status.equals(Status.UNAUTHORIZED)) {
            this.tokenResponse = this.googleAuthCodeFlow.newTokenRequest(code)
                    .setRedirectUri(REDIRECT_URI).execute();
        }
    }

//==============================================================================
// GETTER
//==============================================================================
    public GoogleAuthenticator.Status getStatus(){
        return this.status;
    }

    public String getAuthURL() {
        if(this.status.equals(Status.UNAUTHORIZED) ) {
            return this.googleAuthCodeFlow.newAuthorizationUrl().
                    setRedirectUri(REDIRECT_URI).build();
        }else{
            return null;
        }
    }

    public Drive getService() throws IOException {
        if(this.status.equals(Status.UNAUTHORIZED)) {
            throw new UnAuthorizeException("User not authenticate. Use getAuthURL()" +
                    " to get the authentication url.");
        }

        GoogleCredential cred = makeGoogleCredential();

        if(this.store.containsKey(TOKEN_NAME)){
            log.debug("Token present into the store.");
            StoredCredential sc = this.store.get(TOKEN_NAME);
            cred.setAccessToken(sc.getAccessToken());
            cred.setRefreshToken(sc.getRefreshToken());
        }else{
            log.debug("Token not present into the store.");
            cred.setFromTokenResponse(this.tokenResponse);
            cred.setAccessToken(ACCESS_TOKEN);
            this.store.set(TOKEN_NAME, new StoredCredential(cred));
            this.storeCredential();
            log.debug("Token stored.");
        }

        if(service == null) {
            this.service = new Drive.Builder(this.httpTransport, this.jsonFactory, cred)
                    .setApplicationName(APP_NAME).build();
        }
        this.status = Status.AUTHORIZE;
        return this.service;
    }

//==============================================================================
// INNER CLASS
//==============================================================================
    public enum Status {
        AUTHORIZE,
        UNAUTHORIZED
    }

    public class UnAuthorizeException extends IOException
    {
        public UnAuthorizeException(String m){
            super(m);
        }
    }
}
