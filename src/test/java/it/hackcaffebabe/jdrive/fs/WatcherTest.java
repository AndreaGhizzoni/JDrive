package it.hackcaffebabe.jdrive.fs;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test case for {@link it.hackcaffebabe.jdrive.fs.Watcher}
 */
public class WatcherTest
{
    @Test
    public void testWatcher(){
        Path base = getBasePath();
        if(base == null)
            Assert.fail("Fail to get the base path.");

        Watcher w = retrieveTheWatcher(base);
        if(w == null)
            Assert.fail("Fail to retrieve the Watcher.");
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    public Path getBasePath(){
        try {
            Path base = WatcherUtil.getBase();
            if(!base.toFile().createNewFile())
                return null;
            return base;
        } catch (IOException e) {
           return null;
        }
    }

    public Watcher retrieveTheWatcher(Path base){
        try {
            return Watcher.getInstance(base);
        } catch (IOException e) {
            return  null;
        }
    }
}
