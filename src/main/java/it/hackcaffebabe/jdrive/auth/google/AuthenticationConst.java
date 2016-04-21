package it.hackcaffebabe.jdrive.auth.google;

final class AuthenticationConst
{
    /** Directory to store user credentials for this application. */
    public static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"),
            ".jdrive/cred.json"
    );

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
}
