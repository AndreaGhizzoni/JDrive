package it.hackcaffebabe.jdrive.auth.google;

final class AuthenticationConst
{
    /** Directory to store user credentials for this application. */
    static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"),
            ".jdrive/cred.json"
    );

    static final String APP_NAME = "JDrive";
}
