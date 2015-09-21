package it.hackcaffebabe.jdrive.auth.google;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.api.client.auth.oauth2.StoredCredential;
import it.hackcaffebabe.jdrive.cfg.Default;

import java.io.IOException;
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
    static String toURL( String str ) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    /**
     * Populate the StoredCredential object given.
     * @param s {@link com.google.api.client.auth.oauth2.StoredCredential}
     */
    static void populateStoredCredential( StoredCredential s ) throws IOException {
        JsonParser p = new JsonFactory().createJsonParser(Default.G_TOKEN);

        String fieldName;
        while (p.nextToken() != JsonToken.END_OBJECT) {
            fieldName = p.getCurrentName();
            if(GoogleConst.JSON_AC.equals(fieldName)){
                p.nextToken();
                s.setAccessToken(p.getText());
            }

            if(GoogleConst.JSON_RT.equals(fieldName)){
                p.nextToken();
                s.setRefreshToken(p.getText());
            }
        }
        p.close();
    }
}
