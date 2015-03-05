package it.hackcaffebabe.jdrive.auth.google;

import it.hackcaffebabe.jdrive.Paths;

import java.io.File;

/**
 * TODO add doc
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

    public static final File FILE = new File(Paths.PATH_APP+Paths.SEP+"t.json");
//    private static File JSON_FILE = new File("test/test.json");

//==============================================================================
//  SETTER
//==============================================================================
    /**
     * TODO add doc
     * @param t
     */
//    public static void setTokenFile(File t){
//        TODO check args
//        JSON_FILE = t;
//    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * TODO add doc
     * @return
     */
//    public static File getTokenFile(){
//        return JSON_FILE;
//    }
}
