package it.hackcaffebabe.jdrive;

import java.io.File;

public final class UtilConst {
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
}
