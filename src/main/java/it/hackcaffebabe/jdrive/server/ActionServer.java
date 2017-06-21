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
        log.info("CloserListener started...");
        try {
            log.debug("Try to create a server socket to port: " + SERVER_PORT);
            serverSocket = new ServerSocket( SERVER_PORT, 0, getLocalhost() );
            log.debug("Server Socket created at port "+serverSocket.getLocalPort());

            Socket clientSocket;
            boolean keepListening = true;
            while( keepListening ){
                log.info("Listening for clients...");
                clientSocket = serverSocket.accept();
                log.debug( getClientInfo(clientSocket) );

                BufferedReader inFromClient = new BufferedReader(
                        new InputStreamReader( clientSocket.getInputStream() )
                );

                log.info("Waiting for message from client...");
                String msgFromClient;
                while( (msgFromClient = inFromClient.readLine()) != null ){
                    if( msgFromClient.equals(Message.QUIT) ) {
                        log.debug("quit message received.");
                        keepListening = false;
                        break;
                        // TODO run consumer
                    }
                }

                clientSocket.close();
            }
        } catch (IOException e) {
            log.error( e.getMessage() );
        } finally {
            this.stop();
        }
    }

    private String getClientInfo( Socket clientSocket ){
        return "client connected:" +
               " from: " + clientSocket.getLocalAddress() +
               " port: " + clientSocket.getLocalPort();
    }

    private void stop(){
        try {
            this.serverSocket.close();
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
