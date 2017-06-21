package it.hackcaffebabe.jdrive.action;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * TODO add doc
 */
final class Constants
{
    /** default action port */
    static final int SERVER_PORT = 12345;

    /** return {@link java.net.InetAddress} of localhost */
    static InetAddress getLocalhost(){
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}
        return localhost;
    }
}
