package it.hackcaffebabe.jdrive.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static it.hackcaffebabe.jdrive.server.Constants.SERVER_PORT;
import static it.hackcaffebabe.jdrive.server.Constants.getLocalhost;

/**
 * TODO add doc
 */
public class ActionServer implements Runnable
{
    private static Logger log = LogManager.getLogger();

    private ServerSocket serverSocket;

    @Override
    public void run() {
        log.info("Starting Action Server...");
        try {
            log.debug("Try to create a server socket to port: " + SERVER_PORT);
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

                keepRunning = manageClientMessage( inFromClient );

                clientSocket.close();
            }
        } catch (IOException e) {
            log.error( e.getMessage() );
        } finally {
            this.stop();
        }
    }

    private boolean manageClientMessage( BufferedReader inFromClient )
            throws IOException {
        log.info("Waiting for message from client...");

        String msgFromClient;
        while( (msgFromClient = inFromClient.readLine()) != null ){
            log.debug("message received: " + Message.QUIT);

            if( msgFromClient.equals(Message.QUIT) ) {
                // TODO run consumer
                return false;
            }
        }
        return true;
    }

    private void stop(){
        try {
            log.info("Try to stop Action Server...");
            this.serverSocket.close();
            log.info("Action Server stopped correctly.");
        } catch (IOException e) {
            log.error( e.getMessage() );
        }
    }

    public static void sendQuitRequest() throws IOException {
        Socket socket = new Socket( getLocalhost(), SERVER_PORT );
        PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
        out.append( Message.QUIT ).append( "\n" );
        out.flush();
        out.close();
        socket.close();
    }
}
