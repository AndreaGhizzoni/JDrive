package it.hackcaffebabe.jdrive.action;

import static it.hackcaffebabe.jdrive.Launcher.setPidToThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static it.hackcaffebabe.jdrive.action.Constants.*;

/**
 * ActionServer manage simple actions based on line of text received from
 * localhost. Message must be declared in {@link Message} class as constant.
 */
public class ActionServer implements Runnable
{
    private static Logger log = LogManager.getLogger();

    private ServerSocket serverSocket;
    private HashMap<String, Callable> actions = new HashMap<>();
    private static final Callable DEFAULT = () ->{
        log.error("Message not bound as an action.");
        return "";
    };

    public ActionServer() throws IOException {
        log.debug("Try to create a action socket to port: " + SERVER_PORT);
        this.serverSocket = new ServerSocket(
            SERVER_PORT,
            IGNORE_MAX_PENDING_CONNECTION,
            getLocalhost()
        );
        log.debug("Server Socket created at port "+serverSocket.getLocalPort());
    }

    /**
     * This method adds a new action indexed with string key.
     * @param withMessage {@link java.lang.String} of action
     * @param doThisAction {@link java.util.concurrent.Callable} to take when message
     *               received from localhost is equal to key.
     */
    public void addAction( String withMessage, Callable doThisAction ){
        actions.put( withMessage, doThisAction );
    }

    @Override
    public void run() {
        setPidToThreadContext();
        log.info("Starting Action Server...");

        boolean keepRunning = true;
        while( keepRunning ){
            try {
                log.info("Listening for clients...");
                Socket clientSocket = serverSocket.accept();
                clientSocket.setSoTimeout( INPUT_STREAM_READ_TIMEOUT_MS );
                log.debug(
                    "Client connected: " +
                    "ip: " + clientSocket.getLocalAddress() + " " +
                    "port: " + clientSocket.getLocalPort()
                );

                BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader( clientSocket.getInputStream() )
                );
                BufferedWriter outToClient = new BufferedWriter(
                    new OutputStreamWriter( clientSocket.getOutputStream() )
                );

                keepRunning = manageClientMessage( inFromClient, outToClient );

                clientSocket.close();
            } catch (Exception e) {
                log.error( e.getMessage(), e );
            }
        }
        this.stop();
    }

    private boolean manageClientMessage( BufferedReader inFromClient,
                                         BufferedWriter outToClient )
            throws Exception {
        log.info("Waiting messages from client...");

        String msgFromClient;
        while( (msgFromClient = inFromClient.readLine()) != null ){
            log.debug("message received: " + msgFromClient);
            String computedAction = (String)this.actions
                    .getOrDefault(msgFromClient, DEFAULT).call();
            log.debug("response from action: "+computedAction);

            outToClient.append( computedAction ).append( "\n" );
            outToClient.flush();

            if( msgFromClient.equals(Message.QUIT) ) {
                return false;
            }
        }
        return true;
    }

    private void stop(){
        try {
            log.info("Try to stop Action Server...");
            if( this.serverSocket != null )
                this.serverSocket.close();
            log.info("Action Server stopped correctly.");
        } catch (Exception e) {
            log.error( e.getMessage(), e );
        }
    }
}
