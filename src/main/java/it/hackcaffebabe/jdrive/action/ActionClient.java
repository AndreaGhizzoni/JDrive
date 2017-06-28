package it.hackcaffebabe.jdrive.action;

import java.io.*;
import java.net.Socket;

import static it.hackcaffebabe.jdrive.action.Constants.SERVER_PORT;
import static it.hackcaffebabe.jdrive.action.Constants.getLocalhost;

/**
 * This class provide methods to send and receive message to/from ActionServer.
 */
public class ActionClient
{
    /** Send Message.QUIT to action server. */
    public static void sendQuitRequest() throws IOException {
        send( Message.QUIT );
    }

    /** Send Message.STATUS to action server and return the response. */
    public static String sendStatusRequest() throws IOException {
        return send( Message.STATUS );
    }

    private static String send( String message ) throws IOException {
        Socket socket = new Socket( getLocalhost(), SERVER_PORT );
        BufferedWriter outToServer = new BufferedWriter(
            new OutputStreamWriter( socket.getOutputStream() )
        );
        BufferedReader inFromServer = new BufferedReader(
            new InputStreamReader( socket.getInputStream() )
        );

        outToServer.append( message ).append( "\n" );
        outToServer.flush();

        String response;
        while( (response = inFromServer.readLine()) != null ) {
            break;
        }

        outToServer.close();
        inFromServer.close();
        socket.close();
        return response;
    }
}
