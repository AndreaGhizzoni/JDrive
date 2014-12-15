package it.hackcaffebabe.jdrive.auth.google;

import static javafx.concurrent.Worker.State.FAILED;
import java.awt.*;
import java.io.IOException;
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

/**
 * GUI for {@link it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator} class.
 */
class GoogleAuthenticatorUI implements Runnable
{
    private static final Logger log = LogManager.getLogger("GoogleAuthenticatorUI");

    private JFXPanel jfxPanel;
    private WebEngine engine;
    private JFrame frame = new JFrame();
    private JLabel lblStatus = new JLabel();
    private JProgressBar progressBar = new JProgressBar();

    // used for sync between UI e GoogleAuthenticator class
    // means when the user arrive to the page with authentication code.
    public boolean isFinish = false;

    private GoogleAuthenticator g;

    /**
     * Constructor for GoogleAuthenticator user interface.
     * @param g {@link GoogleAuthenticator}
     */
    public GoogleAuthenticatorUI(GoogleAuthenticator g){
        this.g = g;
    }

    @Override
    public void run() {
        String url = g.getAuthURL();
        if(url==null)
            return;

        frame.setPreferredSize(new Dimension(1024, 600));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
        loadURL(url);
        frame.pack();
        frame.setVisible(true);
    }

    /* initialize all the components */
    private void initComponents() {
        jfxPanel = new JFXPanel();
        createScene();
        progressBar.setStringPainted(true);
        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.EAST);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(jfxPanel, BorderLayout.CENTER);
        panel.add(statusBar, BorderLayout.SOUTH);
        frame.getContentPane().add(panel);
    }

    /* create JavaFX Scene */
    private void createScene() {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                WebView view = new WebView();
                engine = view.getEngine();
                engine.titleProperty().addListener(new TitleChangeListener());
                engine.setOnStatusChanged(new StatusChangeHandler());
                engine.getLoadWorker().stateProperty().addListener(
                        new StateChangeListener());
                engine.getLoadWorker().workDoneProperty().addListener(
                        new WorkDoneProperty());
                engine.getLoadWorker().exceptionProperty().addListener(
                        new ExceptionProperty());
                jfxPanel.setScene(new Scene(view));
            }
        });
    }

    /* package method that allows GoogleAuthenticator class to poll the UI */
    synchronized boolean isProcessFinish(){
        return this.isFinish;
    }

    /* utility method to load the url into the JavaFX main thread */
    private void loadURL(final String url) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                String tmp = Util.toURL(url);
                if (tmp == null)
                    tmp = Util.toURL("http://" + url);
                engine.load(tmp);
            }
        });
    }

//==============================================================================
// INNER CLASS
//==============================================================================
    /* Object that is called every time a page is loaded */
    private class StateChangeListener implements ChangeListener<Worker.State>
    {
        @Override
        public void changed(ObservableValue<? extends Worker.State> observable,
                            Worker.State oldValue, Worker.State newState) {
            if (newState == Worker.State.SUCCEEDED) {
                Document doc = engine.getDocument();
                Element e = doc.getElementById("code");
                if(e != null) {
                    String value = e.getAttribute("value");
                    log.debug("Value :" +value);
                    try{
                        g.setAuthResponseCode(value);
                    }catch (IOException ex){
                        log.error(ex.getMessage());
                    }
                    isFinish = true;
                    frame.dispose();
                }
            }
        }
    }

    /* Object to change the title based on the url loaded */
    private class TitleChangeListener implements ChangeListener<String>
    {
        @Override
        public void changed(
                ObservableValue<? extends String> observable,
                String oldValue,
                final String newValue) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    frame.setTitle(newValue);
                }
            });
        }
    }

    /* Change the status bar */
    private class StatusChangeHandler implements EventHandler<WebEvent<String>>
    {
        @Override
        public void handle(final WebEvent<String> event) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    lblStatus.setText(event.getData());
                }
            });
        }
    }

    /* Change the progress bar loading status */
    private class WorkDoneProperty implements ChangeListener<Number>
    {
        @Override
        public void changed(ObservableValue<? extends Number> observableValue,
                            Number oldValue, final Number newValue) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    progressBar.setValue(newValue.intValue());
                }
            });
        }
    }

    /* Manage some exception */
    private class ExceptionProperty implements ChangeListener<Throwable>
    {
        @Override
        public void changed(ObservableValue<? extends Throwable> o,
                            Throwable old, final Throwable value) {
            if (engine.getLoadWorker().getState() == FAILED) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String l = engine.getLocation()+"\n";
                        String v;
                        if(value != null)
                            v = l + value.getMessage();
                        else
                            v = l +"Unexpected error.";

                        log.error(v);
                        JOptionPane.showMessageDialog(frame.getContentPane(), v,
                                "Loading error...", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        }
    }
}
