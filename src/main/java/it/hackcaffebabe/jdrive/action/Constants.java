package it.hackcaffebabe.jdrive.action;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Constants for package jdrive.action
 */
final class Constants
{
    /** default action port */
    static final int SERVER_PORT = 12345;
    static final int IGNORE_MAX_PENDING_CONNECTION = 0;
    static final int INPUT_STREAM_READ_TIMEOUT_MS = 5000;


    /** return {@link java.net.InetAddress} of localhost */
    static InetAddress getLocalhost(){
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName("localhost");
        } catch (UnknownHostException ignored) {}
        return localhost;
    }
}
