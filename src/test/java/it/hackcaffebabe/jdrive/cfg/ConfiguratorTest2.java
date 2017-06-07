package it.hackcaffebabe.jdrive.cfg;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link it.hackcaffebabe.jdrive.cfg.Configurator}
 */
public class ConfiguratorTest2 {
    @Test
    public void getInstance(){
        try{
            Configurator.getInstance();
            Assert.fail("Configurator.getInstance() must fail without calling " +
                    "Configurator.setup() first.");
        }catch (IllegalStateException ignored){}
    }

    @Test
    public void setupWithPopulatedPropertiesFile(){
        ArrayList<Path> pathsToTest = new ArrayList<>();
        pathsToTest.add( Paths.get("configuration.properties") );
        pathsToTest.add( Paths.get("../configuration.properties") );
        pathsToTest.add( Paths.get("../../configuration.properties") );

        for( Path pathToTest : pathsToTest ){
            createFile(pathToTest);
            populateWithRandomProperties(pathToTest);

            try{
                Configurator c = Configurator.setup(pathToTest);
                checkDefaultValues(c);
            }catch (IllegalArgumentException | ConfigurationException |
                    IOException e){
                Assert.fail( e.getMessage() );
            }finally {
                deleteFile(pathToTest);
            }
        }
    }

    @Test
    public void setupWithEmptyPropertiesFile(){
        ArrayList<Path> pathsToTest = new ArrayList<>();
        pathsToTest.add( Paths.get("configuration.properties") );
        pathsToTest.add( Paths.get("../configuration.properties") );
        pathsToTest.add( Paths.get("../../configuration.properties") );

        for( Path pathToTest : pathsToTest ){
            try{
                Configurator c = Configurator.setup(pathToTest);
                checkDefaultValues(c);
            }catch (IllegalArgumentException | ConfigurationException |
                    IOException e){
                Assert.fail( e.getMessage() );
            }finally {
                deleteFile(pathToTest);
            }
        }
    }

    @Test
    public void setup_Arguments(){
        ArrayList<Path> pathsToTest = new ArrayList<>();
        pathsToTest.add( Paths.get(System.getProperty("java.io.tmpdir")) );
        pathsToTest.add( null );

        for( Path pathToTest : pathsToTest ){
            try{
                Configurator.setup(pathToTest);
                Assert.fail("With "+pathToTest+" as path, setup() must fail.");
            }catch (Exception ignored){}
        }
    }

    @Test
    public void test_PutRemoveExists(){
        HashMap<String, Object> properties = new HashMap<String, Object>(){
            {
                put("MyKey", "AndValue1");
                put("MyKey", "AndValue2");
                put("MyKey", "AndValue3");
                put("OtherKey", "AndAnotherValue");
            }
        };

        Path propertiesFile = Paths.get("prop.properties");
        try{
            Configurator configurator = Configurator.setup(propertiesFile);

            for( Map.Entry<String,Object> prop : properties.entrySet() ){
                configurator.put( prop.getKey(), prop.getValue());

                existKeyInConfigurator( configurator, prop.getKey() );
                checkValueFromKey( configurator, prop.getKey(), prop.getValue() );

                configurator.remove( prop.getKey() );
                if( configurator.exists(prop.getKey()) ){
                    Assert.fail("Property with key="+prop.getKey()+
                            " was removed but method exists() return true.");
                }
            }
        }catch (Exception e){
            Assert.fail( e.getMessage() );
        }finally {
            deleteFile( propertiesFile );
        }
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    private void checkValueFromKey( Configurator c, String actualKey,
                                    Object expectedValue ){
        Object actualValueFromConfigurator = c.get( actualKey );
        Assert.assertEquals(
            "Property ("+actualKey+", "+expectedValue+") " +
                "is not equal to " +
                "("+actualKey+", "+actualValueFromConfigurator+") "+
                "from configurator",
            expectedValue,
            actualValueFromConfigurator
        );
    }

    private void existKeyInConfigurator(Configurator c, String key ){
        Assert.assertTrue(
            "Property with key="+key+" already put must exists in configurator.",
            c.exists( key )
        );
    }

    private void checkDefaultValues( Configurator c ){
        for(Map.Entry<String, Object> def : Default.cfg.entrySet() ){
            Assert.assertTrue(
                "Expected that default key exists into Configurator",
                c.exists( def.getKey() )
            );

            Object defaultValue = def.getValue();
            Object configuratorValue = c.get( def.getKey() );
            Assert.assertEquals(
                    "Expected that loading default cfg file returns the " +
                            "default value",
                    defaultValue, configuratorValue
            );
        }
    }

    private void createFile( Path file ){
        try {
            Files.createFile( file );
        } catch (IOException e) {
            Assert.fail( e.getMessage() );
        }
    }

    private void deleteFile( Path file ){
        try {
            Files.delete( file );
        } catch (IOException e) {
            Assert.fail( e.getMessage() );
        }
    }

    private void populateWithRandomProperties(Path thisPath ){
        try {
            BufferedWriter bw = new BufferedWriter( new FileWriter(
                    thisPath.toFile()
            ));

            int NUMBER_OF_PAIR = 10;
            String format = "key%d = Value%d\n";
            for( int i=0; i<NUMBER_OF_PAIR; i++ ){
                bw.append( String.format(format, i, i) );
            }

            bw.flush();
            bw.close();
        } catch (IOException e) {
            Assert.fail( e.getMessage() );
        }
    }
}
