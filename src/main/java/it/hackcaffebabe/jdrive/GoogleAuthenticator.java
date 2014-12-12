package it.hackcaffebabe.jdrive;

import static it.hackcaffebabe.jdrive.UtilConst.*;
import static it.hackcaffebabe.jdrive.AuthenticationConst.*;

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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/*
 * https://stackoverflow.com/questions/20684238/authorization-to-google-client
 * https://stackoverflow.com/questions/19861178/stored-credential-from-google-api-to-be-reused-java
 */

/**
 * Google Authenticator class.
 * How to use:
 * <pre>{@code
 * GoogleAuthenticator g = GoogleAuthenticator.getInstance();
 * String code;
 * String url = g.getAuthURL();
 * if(url != null){
 *    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 *    code = br.readLine();
 *    g.setAuthResponseCode(code);
 * }
 * Drive service = g.getService();
 * }</pre>
 *
 * @version 0.0.1
 * @author Andrea Ghizzoni
 */
public final class GoogleAuthenticator
{
    private static GoogleAuthenticator instance;
    private static final Logger log = LogManager.getLogger(
            GoogleAuthenticator.class.getSimpleName()
    );

    /**
     * This method returns an instance of GoogleAuthenticator.
     * @return GoogleAuthenticator the GoogleAuthenticator
     * @throws IOException if something goes wrong
     */
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

    /* constructor */
    private GoogleAuthenticator() throws IOException {
        log.entry();
        setStatus(Status.UNAUTHORIZED);
        this.buildHTTPTransportJsonFactory();
        this.buildDataStore();
        this.buildGoogleAuthCodeFlow();
        log.info("Instance created correctly.");
    }

//==============================================================================
// METHOD
//==============================================================================
    /* build up the HTTPTransport and JsonFactory */
    private void buildHTTPTransportJsonFactory() {
        this.httpTransport = new NetHttpTransport();
        this.jsonFactory = new JacksonFactory();
    }

    /* build up the data store and load stored credential if the are any */
    private void buildDataStore() throws IOException{
        this.store = new MemoryDataStoreFactory().getDataStore(STORE_NAME);
        loadCredential();
    }

    /* build up the Google Authentication Flow */
    private void buildGoogleAuthCodeFlow(){
        this.googleAuthCodeFlow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory,
                CLIENT_ID, CLIENT_SECRET,
                Arrays.asList(DriveScopes.DRIVE, DriveScopes.DRIVE_FILE)
        ).setAccessType("offline").setApprovalPrompt("force").build();
    }

    /* store the user credential */
    private void storeCredential() throws IOException{
        if(JSON_FILE.exists() && !JSON_FILE.delete())
            throw new IOException("Error while deleting old authentication token.");

        com.fasterxml.jackson.core.JsonGenerator j = new
                com.fasterxml.jackson.core.JsonFactory().createGenerator(
                JSON_FILE, JsonEncoding.UTF8 );

        StoredCredential c = this.store.get(TOKEN_NAME);
        j.writeStartObject();// {

        j.writeStringField(JSON_AC, c.getAccessToken());
        j.writeStringField(JSON_RT, c.getRefreshToken());

        j.writeEndObject();// }
        j.flush();
        j.close();
    }

    /* load the stored credential */
    private void loadCredential() throws  IOException{
        if(!JSON_FILE.exists())
            return;

        log.info("Credential found: try to load.");
        JsonParser p = new com.fasterxml.jackson.core.JsonFactory()
                   .createJsonParser(JSON_FILE);

        StoredCredential s = new StoredCredential(makeGoogleCredential());

        String fieldName;
        while (p.nextToken() != JsonToken.END_OBJECT) {
            fieldName = p.getCurrentName();
            if(JSON_AC.equals(fieldName)){
                p.nextToken();
                s.setAccessToken(p.getText());
            }

            if(JSON_RT.equals(fieldName)){
                p.nextToken();
                s.setRefreshToken(p.getText());
            }
        }
        p.close();

        this.store.set(TOKEN_NAME, s);
        setStatus(Status.AUTHORIZE);
        log.info("Credential loaded.");
    }

    /* build the GoogleCredential Object */
    private GoogleCredential makeGoogleCredential(){
        return new GoogleCredential.Builder()
                .setTransport(this.httpTransport)
                .setJsonFactory(this.jsonFactory)
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .addRefreshListener(new CredentialRefreshListener() {
                    @Override
                    public void onTokenResponse(Credential credential,
                                                TokenResponse tokenResponse) throws IOException {
                        log.info("Refresh listener: token response.");
                    }
                    @Override
                    public void onTokenErrorResponse(Credential credential,
                                                     TokenErrorResponse tokenErrorResponse) throws IOException {
                        log.info("Refresh listener: token response error.");
                    }
                })
                .build();
    }

//==============================================================================
// SETTER
//==============================================================================
    /**
     * This method is used set the code in response of the authentication url
     * from method <code>getAuthUrl()</code>.
     * @param code {@link String} the response code.
     * @throws IOException if something goes wrong.
     */
    public void setAuthResponseCode(String code) throws IOException{
        if(getStatus().equals(Status.UNAUTHORIZED)) {
            this.tokenResponse = this.googleAuthCodeFlow.newTokenRequest(code)
                    .setRedirectUri(REDIRECT_URI).execute();
            this.status = Status.AUTHORIZE;
        }
    }

    /* set the status of authorizations if argument is not null */
    private void setStatus( GoogleAuthenticator.Status s){
        if(s != null){
            this.status = s;
        }
    }

//==============================================================================
// GETTER
//==============================================================================
    /** @return {@link GoogleAuthenticator.Status} the status of authorization */
    public GoogleAuthenticator.Status getStatus(){
        return this.status;
    }

    /**
     * This method build a url where you can get the authentication code. Once
     * obtained you can pass it to <code>setAuthResponseCode(code)</code>.
     * If <code>getStatus().equals(Status.AUTHORIZED)</code> this method returns
     * null.
     * @return {@link String} the authorization url or null if
     * <code>getStatus().equals(Status.AUTHORIZED)</code>
     */
    public String getAuthURL() {
        if(getStatus().equals(Status.UNAUTHORIZED) ) {
            return this.googleAuthCodeFlow.newAuthorizationUrl().
                    setRedirectUri(REDIRECT_URI).build();
        }else{
            return null;
        }
    }

    /**
     * This method returns the Drive service to manage all Drives functionality.
     * @return {@link Drive} the drive service.
     * @throws IOException or UnAuthorizeException
     */
    public Drive getService() throws IOException {
        if(getStatus().equals(Status.UNAUTHORIZED)) {
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
        setStatus(Status.AUTHORIZE);
        return this.service;
    }


    public void UIAuthentication() throws IOException {
        if(getStatus().equals(Status.AUTHORIZE))
            return;

        SwingUtilities.invokeLater(new UI(this));
    }

//==============================================================================
// INNER CLASS
//==============================================================================
    /** Represents the Authorization status */
    public enum Status { AUTHORIZE, UNAUTHORIZED }

    /**
     * Exception throws when call <code>getService()</code> while your not
     * authenticate with the appropriate procedure.
     */
    public class UnAuthorizeException extends IOException {
        public UnAuthorizeException(String m){ super(m); }
    }

    /**
     * TODO add doc
     */
    private class UI implements Runnable
    {
        private JFXPanel jfxPanel;
        private WebEngine engine;

        private JFrame frame = new JFrame();
        private JPanel panel = new JPanel(new BorderLayout());
        private JLabel lblStatus = new JLabel();
        private JProgressBar progressBar = new JProgressBar();

        private GoogleAuthenticator g;

        public UI(GoogleAuthenticator g){
            this.g = g;
        }

        @Override
        public void run() {
            String url = g.getAuthURL();
            if(url == null)
                return;

            frame.setPreferredSize(new Dimension(1024, 600));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initComponents();
            loadURL(url);

            frame.pack();
            frame.setVisible(true);
        }

        private void initComponents() {
            jfxPanel = new JFXPanel();

            createScene();

            progressBar.setStringPainted(true);

            JPanel statusBar = new JPanel(new BorderLayout(5, 0));
            statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
            statusBar.add(lblStatus, BorderLayout.CENTER);
            statusBar.add(progressBar, BorderLayout.EAST);

            panel.add(jfxPanel, BorderLayout.CENTER);
            panel.add(statusBar, BorderLayout.SOUTH);

            frame.getContentPane().add(panel);
        }

        public void loadURL(final String url) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    String tmp = toURL(url);

                    if (tmp == null) {
                        tmp = toURL("http://" + url);
                    }

                    engine.load(tmp);
                }
            });
        }

        private String toURL(String str) {
            try {
                return new URL(str).toExternalForm();
            } catch (MalformedURLException exception) {
                return null;
            }
        }

        private void createScene() {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    WebView view = new WebView();
                    engine = view.getEngine();

                    engine.titleProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable,
                                            String oldValue, final String newValue) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override public void run() {
                                    frame.setTitle(newValue);
                                }
                            });
                        }
                    });

                    engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                        @Override public void handle(final WebEvent<String> event) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override public void run() {
                                    lblStatus.setText(event.getData());
                                }
                            });
                        }
                    });

                    engine.getLoadWorker().stateProperty()
                            .addListener(new ChangeListener<Worker.State>() {
                                @Override
                                public void changed(ObservableValue<? extends Worker.State> observable,
                                                    Worker.State oldValue, Worker.State newState) {
                                    if (newState == Worker.State.SUCCEEDED) {
                                        Document doc = engine.getDocument();
                                        Element e = doc.getElementById("code");
                                        if (e != null) {
                                            String value = e.getAttribute("value");
                                            log.debug("Value: " + value);
                                            try {
                                                g.setAuthResponseCode(value);
                                                g.getService();
                                            } catch (IOException e1) {
                                                log.error(e1.getMessage());
                                            }
                                            frame.dispose();
                                        }
                                    }
                                }
                            });

                    engine.getLoadWorker().workDoneProperty()
                            .addListener(new ChangeListener<Number>() {
                                @Override
                                public void changed(ObservableValue<? extends Number> observableValue,
                                                    Number oldValue, final Number newValue) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setValue(newValue.intValue());
                                        }
                                    });
                                }
                            });

                    engine.getLoadWorker().exceptionProperty()
                            .addListener(new ChangeListener<Throwable>() {
                                public void changed(ObservableValue<? extends Throwable> o,
                                                    Throwable old, final Throwable value) {
                                    if (engine.getLoadWorker().getState() == Worker.State.FAILED) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                //TODO log this error case
                                                String l = engine.getLocation() + "\n";
                                                String v;
                                                if (value != null)
                                                    v = l + value.getMessage();
                                                else
                                                    v = l + "Unexpected error.";

                                                JOptionPane.showMessageDialog(
                                                        panel, v,
                                                        "Loading error...",
                                                        JOptionPane.ERROR_MESSAGE);
                                            }
                                        });
                                    }
                                }
                            });

                    jfxPanel.setScene(new Scene(view));
                }
            });
        }
    }
}
