package it.hackcaffebabe.jdrive.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import static it.hackcaffebabe.jdrive.action.Constants.SERVER_PORT;
import static it.hackcaffebabe.jdrive.action.Constants.getLocalhost;

/**
 * TODO add doc
 */
public class ActionClient
{
    /** TODO add doc */
    public static void sendQuitRequest() throws IOException {
        Socket socket = new Socket( getLocalhost(), SERVER_PORT );
        PrintWriter out = new PrintWriter( socket.getOutputStream(), true );
        out.append( Message.QUIT ).append( "\n" );
        out.flush();
        out.close();
        socket.close();
    }
}
