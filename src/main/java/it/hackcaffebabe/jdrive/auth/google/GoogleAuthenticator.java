package it.hackcaffebabe.jdrive.auth.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

/**
 * TODO add doc and example
 */
public final class GoogleAuthenticator {

    // instance of singleton
    private static GoogleAuthenticator instance;
    // logger
    private static final Logger log = LogManager.getLogger(
            GoogleAuthenticator.class.getSimpleName()
    );

    /**
     * TODO add doc
     * @return
     */
    public static GoogleAuthenticator getInstance() throws GeneralSecurityException,
            IOException {
        if( instance == null )
            instance = new GoogleAuthenticator();
        return instance;
    }

    // instances needed for Google Authentication process
    private FileDataStoreFactory DATA_STORE_FACTORY;
    private JsonFactory JSON_FACTORY;
    private HttpTransport HTTP_TRANSPORT;
    private GoogleAuthorizationCodeFlow CODE_FLAW;

    // Authenticated object
    private Drive service;

    /* instance of the scopes */
    private static final List<String> SCOPES = Arrays.asList(
            DriveScopes.DRIVE,
            DriveScopes.DRIVE_FILE
    );

    /* constructor */
    private GoogleAuthenticator() throws GeneralSecurityException, IOException {
        log.entry();
        this.buildHTTPTransportJsonFactory();
        this.buildDataStore();
        this.buildGoogleAuthCodeFlow();
        log.debug("GoogleAuthenticator created correctly.");
    }

//==============================================================================
//  METHOD
//==============================================================================
    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public Drive getDriveService() throws IOException {
        log.info("Try to get Google authentication services...");
        if( this.service == null ) {
            Credential credential = new AuthorizationCodeInstalledApp(
                CODE_FLAW,
                new LocalServerReceiver()
            ).authorize("user");
            this.service = new Drive.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(AuthenticationConst.APP_NAME)
                    .build();
        }
        log.info("Google authentication successful.");
        return this.service;
    }

//==============================================================================
//  METHOD
//==============================================================================
    /* build up the HTTPTransport and JsonFactory */
    private void buildHTTPTransportJsonFactory() throws GeneralSecurityException,
            IOException {
        this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.JSON_FACTORY = JacksonFactory.getDefaultInstance();
    }

    /* build up the data store and load stored credential if the are any */
    private void buildDataStore() throws IOException{
        DATA_STORE_FACTORY = new FileDataStoreFactory(
                AuthenticationConst.DATA_STORE_DIR
        );
    }

    /* build up the Google Authentication Flow */
    private void buildGoogleAuthCodeFlow() throws IOException {
        // Load client secrets.
        InputStream in = GoogleAuthenticator.class.getResourceAsStream(
                "/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY, new InputStreamReader(in)
        );
        // Build flow and trigger user authorization request.
        CODE_FLAW = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(DATA_STORE_FACTORY)
            .setAccessType("offline")
            .build();
    }
}


