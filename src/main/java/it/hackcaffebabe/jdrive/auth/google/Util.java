package it.hackcaffebabe.jdrive.auth.google;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class for this set of package class
 */
final class Util
{
    public static final String APP_NAME = "JDrive";
    // key of the token into DataStore
    public static final String TOKEN_NAME = "token";
    // name of the DataStore
    public static final String STORE_NAME = "storeCredential";
    // name of access token
    public static final String ACCESS_TOKEN = "access_token";

    // json stuff to save the authentication token
    public static final String JSON_AC = "at";
    public static final String JSON_RT = "rt";
    public static final File JSON_FILE = new File("test/test.json");

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
