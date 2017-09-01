package it.hackcaffebabe.jdrive.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

import static it.hackcaffebabe.jdrive.action.Constants.SERVER_PORT;
import static it.hackcaffebabe.jdrive.action.Constants.getLocalhost;

/**
 * This class provide methods to send and receive message to/from ActionServer.
 */
public class ActionClient
{
    private static final Logger log = LogManager.getLogger();

    /** Send Message.QUIT to action server. */
    public static void sendQuitRequest() throws IOException {
        send( Message.QUIT );
    }

    /** Send Message.STATUS to action server and return the response. */
    public static String sendStatusRequest() throws IOException {
        return send( Message.STATUS );
    }

    private static String send( String message ) throws IOException {
        log.debug("Try to connect to: "+getLocalhost()+":"+SERVER_PORT);
        Socket socket = new Socket( getLocalhost(), SERVER_PORT );
        log.debug("Connection established");
        BufferedWriter outToServer = new BufferedWriter(
            new OutputStreamWriter( socket.getOutputStream() )
        );
        BufferedReader inFromServer = new BufferedReader(
            new InputStreamReader( socket.getInputStream() )
        );
        log.debug("Streams obtained");

        log.debug("Try to send: "+message);
        outToServer.append( message ).append( "\n" );
        outToServer.flush();
        log.debug("Message sent.");

        log.debug("Waiting for server response...");
        String response;
        while( (response = inFromServer.readLine()) != null ) {
            break;
        }
        log.debug("Server response obtained: "+response);

        log.debug("Closing streams");
        outToServer.close();
        inFromServer.close();
        log.debug("Closing socket");
        socket.close();
        return response;
    }
}
