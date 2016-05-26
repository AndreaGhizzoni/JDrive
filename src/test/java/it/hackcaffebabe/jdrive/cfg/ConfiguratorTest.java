package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
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
        try{
            Configurator.setup(cfg.toFile());
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
        cleanWD();
    }

    @Test
    public void testSetupViaNullFile(){
        try{
            File f = null;
            Configurator.setup(f);
            Assert.fail("Expected to throw an exception if null File is passed"+
                    " to setup() method");
        }catch (Exception ignored){}
        cleanWD();
    }

    @Test
    public void testSetupViaWrongFile(){
        try{
            File dir = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
            Configurator.setup(dir);
            Assert.fail("Expected to throw an exception if directory is passed"+
                    " as argument to setup() method");
        }catch (Exception ignored){}
        cleanWD();
    }

    @Test
    public void testSetupViaPropertiesConfiguration(){
        try {
            PropertiesConfiguration p = new PropertiesConfiguration(
                    cfg.toFile()
            );
            Configurator.setup(p);
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
        cleanWD();
    }

    @Test
    public void testSetupViaNullPropertiesConfiguration(){
        try {
            PropertiesConfiguration p = null;
            Configurator.setup(p);
            Assert.fail("Expected to throw an exception if null " +
                    "PropertiesConfiguration is passed as argument to setup()");
        }catch (Exception ignored){}
        cleanWD();
    }

    @Test
    public void testInstanceViaFile(){
        try{
            Configurator c = Configurator.setup(cfg.toFile());
            Assert.assertTrue(
                    "Expected that setup() method returns a not null instance "+
                            "of Configurator from file setup",
                    c != null
            );
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testInstanceViaPropertiesConfiguration(){
        try{
            PropertiesConfiguration p = new PropertiesConfiguration(
                    cfg.toFile()
            );
            Configurator c = Configurator.setup(p);
            Assert.assertTrue(
                    "Expected that setup() method returns a not null instance "+
                            "of Configurator from file setup",
                    c != null
            );
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
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
    private void buildWD(){
        try {
            Files.createFile(cfg);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    // create a method to clean the working directory
    private void cleanWD(){
        try {
            Files.delete(cfg);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
