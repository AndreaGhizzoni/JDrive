package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit test for {@link it.hackcaffebabe.jdrive.cfg.Configurator}
 */
public class ConfiguratorTest
{
    private static final Path cfg = Paths.get(
            System.getProperty("java.io.tmpdir")+
            System.getProperty("file.separator")+
            "testCFG.conf"
    );

    /* TODO remember to modify buildWD() and cleanWD() method to create and
            delete cfg  */


    @Test
    public void testSetupViaFile(){

    }

    @Test
    public void testSetupViaConfigurationProperties(){

    }

    @Test
    public void testSetupViaNullFile(){

    }

    @Test
    public void testSetupViaWrongFile(){

    }

    @Test
    public void testSetupViaNullConfigurationProperties(){

    }


    @Test
    public void testConfigurator(){
        buildWD();
        Configurator c = Configurator.getInstance();
        c.load();
        c.load(); // testing multiple calling

        Object n = c.get(null);
        Assert.assertNull("Expecting to retrieve null object from null key.", n);
        Object e = c.get("");
        Assert.assertNull("Expecting to retrieve null object from empty key.", e);

        String b = (String)c.get(Keys.WATCHED_DIR);
        Assert.assertEquals("Expecting the same base path.", Default.cfg.get(Keys.WATCHED_DIR), b);

        boolean pNK = c.put(null,1);
        boolean pEK = c.put("",1);
        boolean ok = c.put("testing", 42);

        Assert.assertFalse("Expecting false from put() with null key.", pNK);
        Assert.assertFalse("Expecting false from put() with empty key.", pEK);
        Assert.assertFalse("Expecting false from put() with valid key " +
                "because does not exists before.", ok);

        Integer i = (Integer)c.get("testing");
        Assert.assertEquals("Expecting to retrieve the latest properties",
                new Integer(42), i);

        cleanWD();
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    // create a method to build working directory
    public void buildWD(){
        try {
            PathsUtil.createApplicationHomeDirectory();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    // create a method to clean the working directory
    public void cleanWD(){
        try {
            Files.delete(Paths.get(PathsUtil.APP_CGF_FILE));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
