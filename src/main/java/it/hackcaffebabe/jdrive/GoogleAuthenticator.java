package it.hackcaffebabe.jdrive;

import static it.hackcaffebabe.jdrive.UtilConst.*;
import static it.hackcaffebabe.jdrive.AuthConst.*;

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

import java.io.IOException;
import java.util.Arrays;

public final class GoogleAuthenticator
{
    private static GoogleAuthenticator instance;

    public static GoogleAuthenticator getInstance() throws IOException {
        if (instance == null)
            instance = new GoogleAuthenticator();
        return instance;
    }

    private HttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private GoogleAuthorizationCodeFlow googleAuthCodeFlow;
    private DataStore<StoredCredential> store;
    private Drive service;

    private GoogleAuthenticator() throws IOException {
        this.buildHTTPTransportJsonFactory();
        this.buildGoogleAuthCodeFlow();
    }

//==============================================================================
// METHOD
//==============================================================================
    private void buildHTTPTransportJsonFactory() {
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = new JacksonFactory();
    }

    private void buildGoogleAuthCodeFlow() throws IOException {
        this.store = new MemoryDataStoreFactory().getDataStore("store");
        this.googleAuthCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                CLIENT_ID,
                CLIENT_SECRET,
                Arrays.asList(DriveScopes.DRIVE)
        )
         .setAccessType("offline")
         .setApprovalPrompt("force").build();
    }

//==============================================================================
// GETTER
//==============================================================================
    public String getAuthURL() {
        return this.googleAuthCodeFlow.newAuthorizationUrl().
                setRedirectUri(REDIRECT_URI).build();
    }

    public Drive getService(String auth) throws IOException {
        GoogleCredential cred = new GoogleCredential.Builder()
                .setTransport(this.httpTransport)
                .setJsonFactory(this.jsonFactory)
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .addRefreshListener(new CredentialRefreshListener() {
                    @Override
                    public void onTokenResponse(Credential credential,
                                                TokenResponse tokenResponse) throws IOException {
                        System.out.println("successfully");
                    }
                    @Override
                    public void onTokenErrorResponse(Credential credential,
                                                     TokenErrorResponse tokenErrorResponse) throws IOException {
                        System.out.println("wrong");
                    }
                })
                .build();

        if(this.store.containsKey("stored")){
            System.out.println("token stored");
            StoredCredential sc = this.store.get("stored");
            cred.setAccessToken(sc.getAccessToken());
            cred.setRefreshToken(sc.getRefreshToken());
        }else{
            GoogleTokenResponse t = this.googleAuthCodeFlow.newTokenRequest(auth)
                .setRedirectUri(REDIRECT_URI).execute();
            cred.setFromTokenResponse(t);
            cred.setAccessToken("access_token");
            this.store.set("stored", new StoredCredential(cred));
        }

//        GoogleCredential cred = new GoogleCredential.Builder()
//                .setTransport(this.httpTransport)
//                .setJsonFactory(this.jsonFactory)
//                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
//                .addRefreshListener(new CredentialRefreshListener() {
//                    @Override
//                    public void onTokenResponse(Credential credential,
//                                                TokenResponse tokenResponse) throws IOException {
//                        System.out.println("successfully");
//                    }
//                    @Override
//                    public void onTokenErrorResponse(Credential credential,
//                                                     TokenErrorResponse tokenErrorResponse) throws IOException {
//
//                        System.out.println("wrong");
//                    }
//                })
//                .build()
//                .setFromTokenResponse(t)
//                .setAccessToken("access_token");

        this.service = new Drive.Builder(this.httpTransport, this.jsonFactory, cred)
                .setApplicationName(APP_NAME).build();
        return this.service;
    }
}
