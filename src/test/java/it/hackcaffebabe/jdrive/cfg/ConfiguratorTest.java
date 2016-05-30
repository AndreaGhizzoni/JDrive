package it.hackcaffebabe.jdrive.cfg;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Unit test for {@link it.hackcaffebabe.jdrive.cfg.Configurator}
 */
public class ConfiguratorTest
{
    private static final Path cfg = Paths.get(
            System.getProperty("java.io.tmpdir")+
            System.getProperty("file.separator")+
            "defCFG.conf"
    );

    private static final Path custom = Paths.get(
            System.getProperty("java.io.tmpdir")+
            System.getProperty("file.separator")+
            "customFG.conf"
    );

    @Test
    public void testSetupViaFile(){
        // testing if cfg does not exists
        try{
            Configurator.setup(cfg.toFile());
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
        deleteDefaultCFG();

        // testing if cfg exists
        createDefaultCFG();
        try{
            Configurator.setup(cfg.toFile());
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
        deleteDefaultCFG();
    }

    @Test
    public void testSetupViaNullFile(){
        // in this test is not important that cfg file exists or not.
        try{
            File f = null;
            Configurator.setup(f);
            Assert.fail("Expected to throw an exception if null File is passed"+
                    " to setup() method");
        }catch (Exception ignored){}
    }

    @Test
    public void testSetupViaWrongFile(){
        // in this test is not important that cfg file exists or not.
        try{
            File dir = Paths.get(System.getProperty("java.io.tmpdir")).toFile();
            Configurator.setup(dir);
            Assert.fail("Expected to throw an exception if directory is passed"+
                    " as argument to setup() method");
        }catch (Exception ignored){}
    }

    @Test
    public void testSetupViaPropertiesConfiguration(){
        // testing if cfg does not exists
        try {
            PropertiesConfiguration p = new PropertiesConfiguration(
                    cfg.toFile()
            );
            Configurator.setup(p);
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
        deleteDefaultCFG();

        // testing if cfg exists
        createDefaultCFG();
        try {
            PropertiesConfiguration p = new PropertiesConfiguration(
                    cfg.toFile()
            );
            Configurator.setup(p);
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
        deleteDefaultCFG();
    }

    @Test
    public void testSetupViaNullPropertiesConfiguration(){
        try {
            PropertiesConfiguration p = null;
            Configurator.setup(p);
            Assert.fail("Expected to throw an exception if null " +
                    "PropertiesConfiguration is passed as argument to setup()");
        }catch (Exception ignored){}
    }

    @Test
    public void testInstanceViaFile(){
        // testing if cfg does not exists
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
        deleteDefaultCFG();

        // testing if cfg exists
        createDefaultCFG();
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
        deleteDefaultCFG();
    }

    @Test
    public void testInstanceViaPropertiesConfiguration(){
        // testing if cfg does not exists
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
        deleteDefaultCFG();

        // testing if cfg exists
        createDefaultCFG();
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
        deleteDefaultCFG();
    }

    @Test
    public void testIOFromDefaultProperties(){
        Configurator c = null;
        try{
            c = Configurator.setup(cfg.toFile());
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }

        for(Map.Entry<String, Object> t : Default.cfg.entrySet() ){
            Assert.assertTrue(
                    "Expected that default key exists into Configurator when " +
                            "loads defaults cfg",
                    c.exists(t.getKey())
            );

            Object valueFromDefault = t.getValue();
            Object valueFromConfigutator = c.get(t.getKey());
            boolean mustBeTrue = valueFromConfigutator.toString().equals(
                    valueFromDefault.toString()
            );
            Assert.assertTrue(
                    "Expected that loading default cfg file returns the " +
                            "default value",
                    mustBeTrue
            );
        }

        testGetAndPutValue(c);
        testGetAndPutWrongValue(c);
        deleteDefaultCFG();
    }

    @Test
    public void testIOFromCustomPropertiesFile(){
        createTestPropertiesFile();

        Configurator c = null;
        try{
            c = Configurator.setup(custom.toFile());
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }

        boolean existsKey1 = c.exists("Key1");
        Assert.assertTrue(
                "Expect that the first key of custom properties file actual " +
                        "exists into file.",
                existsKey1
        );

        Object valueKey1 = c.get("Key1");
        Assert.assertTrue(
                "Expect to retrive custom value from custom properties file",
                valueKey1.toString().equals("Value1")
        );

        testGetAndPutValue(c);
        testGetAndPutWrongValue(c);
        deleteTestPropertiesFile();
    }

    // method that test put() and get() method when Configurator loads default
    // properties or custom properties.
    public void testGetAndPutValue(Configurator c){
        String newKey = new Long(System.nanoTime()).toString();
        String newValueForNewKey = "My fantastic value";
        boolean expectFalse = c.put(newKey, newValueForNewKey);
        Assert.assertFalse(
                "Expect that there isn't other keys equal to: "+newKey,
                expectFalse
        );

        boolean newKeyExists = c.exists(newKey);
        Assert.assertTrue(
                "Expect that new key actual exists in default properties file",
                newKeyExists
        );

        String newValueFromConfigurator = c.get(newKey).toString();
        Assert.assertTrue(
                "Expect that set new custom value returns the same value from "+
                        "default configurator",
                newValueForNewKey.equals(newValueFromConfigurator)
        );

        c.remove(newKey);
        boolean expectFalseAfterRemove = c.exists(newKey);
        Assert.assertFalse(
                "Expect that after delete a key, there isn't in the default " +
                        "properties file anymore",
                expectFalseAfterRemove
        );

        Object mustBeNull = c.get(newKey);
        Assert.assertNull(
                "Expect null object from a deleted key",
                mustBeNull
        );
    }

    // method that test put() and get() method with wrong values when
    // Configurator loads custom properties or custom properties.
    public void testGetAndPutWrongValue(Configurator c){
        boolean falseOnPutNull = c.put(null, "whatever");
        boolean falseOnPutEmptyString = c.put("", "whatever");
        Assert.assertFalse(
                "Expect false when putting null key into Configurator",
                falseOnPutNull
        );
        Assert.assertFalse(
                "Expect false when putting empty string as key into " +
                        "Configurator",
                falseOnPutEmptyString
        );

        // TODO | to test remove(null) and remove("") -> do a method
        // TODO | c.getEntrySet() that returns all the (key, value) in the
        // TODO | properties file. In this way I can put new (key, value),
        // TODO | call remove(null) and remove("") and see that the entry set is
        // TODO | not changed.

        boolean falseOnExsistsNull = c.exists(null);
        Assert.assertFalse(
                "Expect that doesn't exists any key as null",
                falseOnExsistsNull
        );
        boolean falseOnExistsEmptyString = c.exists("");
        Assert.assertFalse(
                "Expect that doesn't exists any key as empty string",
                falseOnExistsEmptyString
        );

        Object nullObjectOnGetNull = c.get(null);
        Assert.assertNull(
                "Expect null object from getting null key",
                nullObjectOnGetNull
        );
        Object nullObjectOnGetEmptyString = c.get("");
        Assert.assertNull(
                "Expect null object from getting empty key",
                nullObjectOnGetEmptyString
        );
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    // create a method to build working directory
    private void createDefaultCFG(){
        try {
            Files.createFile(cfg);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    // create a method to clean the working directory
    private void deleteDefaultCFG(){
        try {
            Files.delete(cfg);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    // method to create example of properties file
    private void createTestPropertiesFile(){
        try {
            Files.createFile(custom);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter(custom.toFile())
            );
            bw.append("Key1:Value1");
            bw.flush();
            bw.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    // method to delete custom test properties file
    private void deleteTestPropertiesFile(){
        try {
            Files.delete(custom);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }
}
