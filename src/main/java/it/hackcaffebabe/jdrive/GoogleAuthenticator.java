package it.hackcaffebabe.jdrive;

import static it.hackcaffebabe.jdrive.UtilConst.*;
import static it.hackcaffebabe.jdrive.AuthConst.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.util.Arrays;

public final class GoogleAuthenticator
{
    private static GoogleAuthenticator instance;
    public static GoogleAuthenticator getInstance(){
        if(instance == null)
            instance = new GoogleAuthenticator();
        return instance;
    }

    private HttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private GoogleAuthorizationCodeFlow googleAuthCodeFlow;
    private Drive service;

    private GoogleAuthenticator(){
        this.buildHTTPTransportJsonFactory();
        this.buildGoogleAuthCodeFlow();
    }

//==============================================================================
// METHOD
//==============================================================================
    private void buildHTTPTransportJsonFactory(){
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = new JacksonFactory();
    }

    private void buildGoogleAuthCodeFlow(){
         this.googleAuthCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                 this.httpTransport, this.jsonFactory, CLIENT_ID, CLIENT_SECRET,
                 Arrays.asList(DriveScopes.DRIVE)).setAccessType("online")
                .setApprovalPrompt("auto").build();
    }

//==============================================================================
// GETTER
//==============================================================================
    public String getAuthURL(){
        return this.googleAuthCodeFlow.newAuthorizationUrl().
                setRedirectUri(REDIRECT_URI).build();
    }

    public Drive getService(String auth) throws IOException {
        GoogleTokenResponse t = this.googleAuthCodeFlow.newTokenRequest(auth)
                .setRedirectUri(REDIRECT_URI).execute();
        GoogleCredential c = new GoogleCredential().setFromTokenResponse(t);
        this.service = new Drive.Builder(this.httpTransport, this.jsonFactory, c)
                .setApplicationName(APP_NAME).build();
        return this.service;
    }

}
