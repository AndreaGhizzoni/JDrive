package it.hackcaffebabe.jdrive.auth.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.Drive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.security.GeneralSecurityException;

/**
 * Simple class to get an authenticate object from Google and using Drive API
 * How to use:<pre>{@code
 * try{
 *      GoogleAuthenticator.getInstance().getDriveService();
 * } catch (IOException | GeneralSecurityException e) {
 *      e.printStackTrace();
 * }
 * }</pre>
 */
public final class GoogleAuthenticator {

    // instance of singleton
    private static GoogleAuthenticator instance;
    // logger
    private static final Logger log = LogManager.getLogger();

    /**
     * Instances a new Google Authenticator object.
     * @return {@link GoogleAuthenticator} the object to provide the authentication
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

    /* constructor */
    private GoogleAuthenticator() throws GeneralSecurityException, IOException {
        log.info("Try to build GoogleAuthenticator...");
        this.buildHTTPTransportJsonFactory();
        this.buildDataStore();
        this.buildGoogleAuthCodeFlow();
        log.info("GoogleAuthenticator created correctly.");
    }

//==============================================================================
//  METHOD
//==============================================================================
    /**
     * Build and return an authorized Drive object for Google service.
     * @return {@link Drive} the authorized client service
     * @throws IOException if something went wrong during the process
     */
    public Drive getDriveService() throws IOException {
        log.info("Checking internet connection...");
        if( !GoogleAuthenticator.isInternetConnectionAvailable() ){
            throw new IOException("Internet Connection not present or timeout "+
            "exceeded.");
        }
        log.info("Internet connection ok.");

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
                HTTP_TRANSPORT,
                JSON_FACTORY,
                clientSecrets,
                AuthenticationConst.SCOPES
        ).setDataStoreFactory(DATA_STORE_FACTORY)
         .setAccessType("offline")
         .build();
    }

    /**
     * check internet connection by sending ICMP Echo Request to 8.8.8.8.
     * @return true if destination 8.8.8.8 is reachable, false otherwise
     * @throws IOException if destination is not reachable or request is timed
     * out
     */
    public static boolean isInternetConnectionAvailable() throws IOException{
        Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 8.8.8.8");
        try {
            return p1.waitFor()==0;
        } catch (InterruptedException e) {
            throw new IOException("Error while checking internet connection.");
        }
    }
}


