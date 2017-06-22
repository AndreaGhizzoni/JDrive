package it.hackcaffebabe.jdrive.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static it.hackcaffebabe.jdrive.action.Constants.SERVER_PORT;
import static it.hackcaffebabe.jdrive.action.Constants.getLocalhost;

/**
 * TODO add doc
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

    public void putAction( String key, Callable action ){
        actions.put( key, action );
    }

    @Override
    public void run() {
        log.info("Starting Action Server...");
        try {
            log.debug("Try to create a action socket to port: " + SERVER_PORT);
            serverSocket = new ServerSocket( SERVER_PORT, 0, getLocalhost() );
            log.debug("Server Socket created at port "+serverSocket.getLocalPort());

            Socket clientSocket;
            boolean keepRunning = true;
            while( keepRunning ){
                log.info("Listening for clients...");
                clientSocket = serverSocket.accept();
                log.debug(
                    "Client connected:" +
                    " ip: " + clientSocket.getLocalAddress() +
                    " port: " + clientSocket.getLocalPort()
                );

                BufferedReader inFromClient = new BufferedReader(
                    new InputStreamReader( clientSocket.getInputStream() )
                );
                BufferedWriter outToClient = new BufferedWriter(
                    new OutputStreamWriter( clientSocket.getOutputStream() )
                );

                keepRunning = manageClientMessage( inFromClient, outToClient );

                clientSocket.close();
            }
        } catch (Exception e) {
            log.error( e.getMessage() );
        } finally {
            this.stop();
        }
    }

    private boolean manageClientMessage( BufferedReader inFromClient,
                                         BufferedWriter outToClient )
            throws Exception {
        log.info("Waiting for message from client...");

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
        } catch (IOException e) {
            log.error( e.getMessage() );
        }
    }
}
