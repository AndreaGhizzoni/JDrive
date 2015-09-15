package it.hackcaffebabe.jdrive.auth.google;

import it.hackcaffebabe.jdrive.cfg.Default;
import it.hackcaffebabe.jdrive.util.PathsUtil;

import java.io.File;

/**
 * Class to hold all the Google token constant.
 */
public final class TokenConst
{
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
