package it.hackcaffebabe.jdrive.cfg;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Unit test for {@link it.hackcaffebabe.jdrive.cfg.Configurator}
 */
public class ConfiguratorTest
{
    private Path p = java.nio.file.Paths.get("/tmp/testing");

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
            Files.createDirectories(this.p);
        } catch (IOException e) {
            Assert.fail("Fail to build wd.");
        }
    }

    // create a method to clean the working directory
    public void cleanWD(){
        try {
            Files.delete(this.p);
        } catch (IOException e) {
            Assert.fail("Fail to clean wd.");
        }
    }
}
