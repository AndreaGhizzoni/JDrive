package it.hackcaffebabe.jdrive.auth.google;

import static javafx.concurrent.Worker.State.FAILED;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
 * TODO add doc
 */
public class GoogleAuthenticatorUI implements Runnable
{
    private static final Logger log = LogManager.getLogger("GoogleAuthenticatorUI");
    private JFXPanel jfxPanel;
    private WebEngine engine;

    private JFrame frame = new JFrame();
    private JPanel panel = new JPanel(new BorderLayout());
    private JLabel lblStatus = new JLabel();

    private JProgressBar progressBar = new JProgressBar();

    private GoogleAuthenticator g;

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
            @Override public void run() {
                String tmp = toURL(url);

                if (tmp == null) {
                    tmp = toURL("http://" + url);
                }

                engine.load(tmp);
            }
        });
    }

    private static String toURL(String str) {
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
                                    if(e != null) {
                                        String value = e.getAttribute("value");
                                        log.debug("Value :" +value);
                                        try{
                                            g.setAuthResponseCode(value);
                                            g.getService();
                                        }catch (IOException ex){
                                            log.error(ex.getMessage());
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
                            @Override public void run() {
                                progressBar.setValue(newValue.intValue());
                            }
                        });
                    }
                });

                engine.getLoadWorker().exceptionProperty()
                        .addListener(new ChangeListener<Throwable>() {
                            public void changed(ObservableValue<? extends Throwable> o,
                                                Throwable old, final Throwable value) {
                                if (engine.getLoadWorker().getState() == FAILED) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override public void run() {
                                            //TODO log this error case
                                            String l = engine.getLocation()+"\n";
                                            String v;
                                            if(value != null)
                                                v = l + value.getMessage();
                                            else
                                                v = l +"Unexpected error.";

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
