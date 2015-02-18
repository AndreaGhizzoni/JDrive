package it.hackcaffebabe.jdrive.cfg;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit test for {@link it.hackcaffebabe.jdrive.cfg.Configurator}
 */
public class ConfiguratorTest
{
    private Path p = Paths.get("/tmp/testing");

    @Test
    public void testConfigurator(){
        buildWD();

        cleanWD();
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    // create a method that spawn a configuration test file
    public void buildConfigurationTest(){
        try{
            Path file = Paths.get("/tmp/testing/cfg.config");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file.toFile()));
            bw.write("test = 42");
            bw.newLine();
            bw.write("my.string = hello");
            bw.newLine();
            bw.write("my.path = /path/to/star");
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }


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
