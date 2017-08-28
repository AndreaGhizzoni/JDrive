package it.hackcaffebabe.jdrive.remote.google.auth;

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

import com.google.api.services.drive.Drive;
import it.hackcaffebabe.jdrive.util.NetworkUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

/**
 * Simple class to get an authenticate object from Google and using Drive API
 * How to use:<pre>{@code
 * try{
 *     GoogleAuthenticator g = GoogleAuthenticator.getInstance();
 *     g.authenticate();
 *     Drive d = g.getDriveService();
 * } catch (IOException | GeneralSecurityException e) {
 *      e.printStackTrace();
 * }
 * }</pre>
 */
public final class GoogleAuthenticator
{
    private static final Logger log = LogManager.getLogger();

    private static GoogleAuthenticator instance;

    // instances needed for Google Authentication process
    private FileDataStoreFactory DATA_STORE_FACTORY;
    private JsonFactory JSON_FACTORY;
    private HttpTransport HTTP_TRANSPORT;
    private GoogleAuthorizationCodeFlow CODE_FLAW;
    private Credential credential;

    // Authenticated object
    private Drive service;

    /**
     * Instances a new Google Authenticator object.
     * @return {@link GoogleAuthenticator}
     *         the object to provide the authentication.
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static GoogleAuthenticator getInstance()
            throws GeneralSecurityException, IOException {
        if( instance == null )
            instance = new GoogleAuthenticator();
        return instance;
    }

    private GoogleAuthenticator() throws GeneralSecurityException, IOException {
        log.info("Try to build GoogleAuthenticator...");
        this.buildHTTPTransportJsonFactory();
        this.buildDataStore();
        this.buildGoogleAuthCodeFlow();
        log.info("GoogleAuthenticator created correctly.");
    }

    /**
     * This method try to authenticate with Google using Google Authentication
     * process.
     * @throws IOException if something went wrong during the process or
     * there is no internet connection.
     */
    public void authenticate() throws IOException {
        this.checkInternetConnection();

        log.info("Try to authenticate with Google...");
        if( this.credential == null ) {
            this.credential = new AuthorizationCodeInstalledApp(
                CODE_FLAW,
                new LocalServerReceiver()
            ).authorize("user");
        }
        log.info("Google authentication successful.");
    }

    /**
     * This method return the Drive authenticate object.
     * @return {@link Drive} the Google authenticate object
     * @throws IOException if something went wrong during the process or
     *         there is no internet connection.
     */
    public Drive getDriveService() throws IOException {
        if( this.credential == null ) {
            this.authenticate();
        }else {
            this.checkInternetConnection();
        }

        log.info("Try to get Google Drive services...");
        if( this.service == null ) {
            this.service = new Drive.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                credential
            ).setApplicationName(AuthenticationConst.APP_NAME).build();
        }
        log.info("Google Drive service get.");
        return this.service;
    }

//==============================================================================
//  METHOD
//==============================================================================
    private void buildHTTPTransportJsonFactory()
            throws GeneralSecurityException, IOException {
        log.debug("build HTTP Transport JSON Factory...");
        this.HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.JSON_FACTORY = JacksonFactory.getDefaultInstance();
        log.debug("build HTTP Transport JSON Factory...ok");
    }

    private void buildDataStore() throws IOException{
        log.debug("build Data Store...");
        DATA_STORE_FACTORY = new FileDataStoreFactory(
                new File( AuthenticationConst.DATA_STORE_JSON )
        );
        log.debug("build Data Store...ok");
    }

    private void buildGoogleAuthCodeFlow() throws IOException {
        log.debug("build GoogleAuthCodeFlow...");
        // Load client secrets.
        InputStream in = GoogleAuthenticator.class.getResourceAsStream(
                AuthenticationConst.LOCAL_CRED
        );
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
        log.debug("build GoogleAuthCodeFlow...ok");
    }

    /* if there isn't internet connection available throw an Exception */
    private void checkInternetConnection() throws IOException {
        log.info("Checking internet connection...");
        NetworkUtil.isInternetAvailableOrThrow();
        log.info("Internet connection ok.");
    }
}
