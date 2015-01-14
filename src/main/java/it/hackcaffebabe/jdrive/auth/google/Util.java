package it.hackcaffebabe.jdrive.auth.google;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class for this set of package class
 */
final class Util
{
    public static final String APP_NAME = "JDrive";

    /**
     * Convert a string in {@link URL}.
     * @param str {@link String}
     * @return the URL of null if MalformedURLException is thrown.
     */
    static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }
}
