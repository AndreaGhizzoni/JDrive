package it.hackcaffebabe.jdrive.util;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Utility class to manage network situations
 */
public final class NetworkUtil
{
    private static final String DEFAULT_TEST_IP = "8.8.8.8";
    private static final int DEFAULT_TIMEOUT_IN_MS = 200;

    /**
     * This method check if internet connection is available by sending ICMP
     * Echo request to 8.8.8.8. If request fail or timeout of 200ms expire
     * throw an IOException, does nothing otherwise.
     * @throws IOException if timeout expire or request fail.
     */
    public static void isInternetAvailableOrThrow() throws IOException {
        isInternetAvailableOrThrow( DEFAULT_TIMEOUT_IN_MS );
    }

    /**
     * This method check if internet connection is available by sending ICMP
     * Echo request to 8.8.8.8. If request fail or timeout expire throw an
     * IOException, does nothing otherwise.
     * @throws IOException if timeout expire or request fail.
     */
    public static void isInternetAvailableOrThrow( int timeout )
            throws IOException {
        tryToReachDefaultDestination( timeout );
    }

    /**
     * Check internet connection by sending ICMP Echo Request to 8.8.8.8.
     * @return true if destination 8.8.8.8 is reachable, false otherwise.
     */
    public static boolean isInternetConnectionAvailable( ) {
        return isInternetConnectionAvailable( DEFAULT_TIMEOUT_IN_MS );
    }

    /**
     * Check internet connection by sending ICMP Echo Request to 8.8.8.8.
     * @return true if destination 8.8.8.8 is reachable, false otherwise and if
     *              timeout given is negative
     */
    public static boolean isInternetConnectionAvailable( int timeout ){
        try {
            return tryToReachDefaultDestination( timeout );
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean tryToReachDefaultDestination( int timeout )
            throws IOException {
        return InetAddress.getByName( DEFAULT_TEST_IP ).isReachable( timeout );
    }
}
