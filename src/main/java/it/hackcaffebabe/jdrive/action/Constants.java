package it.hackcaffebabe.jdrive.action;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * TODO add doc
 */
public final class Constants
{
    /** default action port */
    public static final int SERVER_PORT = 12345;

    /** return {@link java.net.InetAddress} of localhost */
    public static InetAddress getLocalhost(){
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}
        return localhost;
    }
}
