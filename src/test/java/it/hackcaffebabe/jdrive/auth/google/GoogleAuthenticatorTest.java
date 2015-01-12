package it.hackcaffebabe.jdrive.auth.google;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import static it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator.Status;

/**
 * Test case for {@link it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator}
 */
public class GoogleAuthenticatorTest
{
    @Test
    public void testGoogleAuthenticator(){
        GoogleAuthenticator g = makeGA();
        Assert.assertNotNull("getInstance() not returns null", g);

        if(canGetService(g))
            Assert.fail("Can retrieve the Drive service just after calling " +
                    "getInstance()");

        Assert.assertSame("Instancing twice returns the same object", g, makeGA());
        Assert.assertTrue("Status of GoogleAuthenticator just instanced is " +
                        "unauthorized", g.getStatus().equals(Status.UNAUTHORIZED));

        String url = g.getAuthURL();
        Assert.assertNotNull("Authentication URL can not be null", url);

        if(canGetService(g))
            Assert.fail("Can retrieve the Drive service just after getAuthURL()");

        String notURL = "blablabla";
        String maybeURL = "aasdubu.aa/asdhh8d";
        String someUrl = "http://google.com";
        if(canSetInvalidAuthResponseCode(g, null))
            Assert.fail("I can set authorization url as null string");
        if(canSetInvalidAuthResponseCode(g, ""))
            Assert.fail("I can set authorization url as empty string");
        if(canSetInvalidAuthResponseCode(g, notURL))
            Assert.fail("I can set authorization url as not a URL");
        if(canSetInvalidAuthResponseCode(g, maybeURL))
            Assert.fail("I can set authorization url as not a URL");
        if(canSetInvalidAuthResponseCode(g, someUrl))
            Assert.fail("I can set authorization url as not a the right URL");
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    private static boolean canSetInvalidAuthResponseCode( GoogleAuthenticator g,
                                                          String c){
        try {
            g.setAuthResponseCode(c);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private static boolean canGetService(GoogleAuthenticator g){
       try{
           g.getService();
           return true;
       }catch (GoogleAuthenticator.UnAuthorizeException e){
           return false;
       } catch (IOException e) {
           return false;
       }
    }

    private static GoogleAuthenticator makeGA(){
       try {
           return GoogleAuthenticator.getInstance();
       }catch (Exception e){
           return null;
       }
    }
}