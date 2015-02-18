package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.Paths;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;

/**
 * Unit test for {@link it.hackcaffebabe.jdrive.cfg.Configurator}
 */
public class ConfiguratorTest
{
    @Test
    public void testConfigurator(){
        buildWD();

        cleanWD();
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    // create a method to build working directory
    public void buildWD(){
        try {
            Paths.buildWorkingDirectory();
        } catch (IOException e) {
            Assert.fail("Fail to build wd.");
        }
    }

    // create a method to clean the working directory
    public void cleanWD(){
        try {
            Files.delete(java.nio.file.Paths.get(Paths.PATH_APP));
        } catch (IOException e) {
            Assert.fail("Fail to clean wd.");
        }
    }
}
