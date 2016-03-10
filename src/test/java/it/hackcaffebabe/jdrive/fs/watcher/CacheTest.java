package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Test case for {@link Cache}
 */
public class CacheTest {

    @Test
    public void testWatcherTest(){
        makeAPPHOME();
        makeCacheFile();

        Cache cache = getInstance();
        Assert.assertTrue("WatcherCacheImpl.getInstance() return something != null",
                cache != null);

        try{
            cache.put(null, 1L);
            Assert.fail("I can set null path as key");
        }catch (IllegalArgumentException ignored){}

        try{
            cache.put(Paths.get("/tmp"), null);
            Assert.fail("I can set null value of key");
        }catch (IllegalArgumentException ignored){}

        try{
            cache.get(null);
            Assert.fail("I can pass null as key to retrieve his value");
        }catch (IllegalArgumentException ignored){}

        try{
            cache.remove(null);
            Assert.fail("I can pass null as key to remove his value");
        }catch (IllegalArgumentException ignored){}

        try{
            cache.isWatched(null);
            Assert.fail("I can pass null as key to check if his value is present");
        }catch (IllegalArgumentException ignored){}

        int savedPaths = cache.getCachedPaths().size();
        Assert.assertTrue("Saved Paths must be two", savedPaths == 2);

        Path p1 = Paths.get("/tmp/a.txt");
        Assert.assertTrue("isWatched() must return true for a saved key", cache.isWatched(p1));
        Long valueOfP1 = cache.get(p1);
        Assert.assertTrue("getting value from existing key does not return null",
                valueOfP1 != null);
        Long expected = 1L;
        Assert.assertEquals("value expected form get() must be: "+expected,
                expected, valueOfP1);

        Path p2 = Paths.get("/tmp/b.txt");
        Assert.assertTrue("isWatched() must return true for a saved key", cache.isWatched(p2));
        Long valueOfP2 = cache.get(p2);
        Assert.assertTrue("getting value from existing key does not return null",
                valueOfP2 != null);
        expected = 2L;
        Assert.assertEquals("value expected from get() must be: "+expected,
                expected, valueOfP2);

        Path newPath = Paths.get("/tmp/c.txt");
        Long newLong = 3L;
        Assert.assertTrue("When save a new value, put() returns null",
                cache.put(newPath, newLong) == null );

        Long retriedValue = cache.get(newPath);
        Assert.assertTrue("get a existing key does not return null", retriedValue != null);
        Assert.assertEquals("value expected from get() must be: "+newLong,
                newLong, retriedValue);

        savedPaths = cache.getCachedPaths().size();
        cache.remove(newPath);
        Assert.assertTrue("Cached Paths size (after removing one) not match.",
                savedPaths == cache.getCachedPaths().size()+1);

//        try{
//            cache.flush();
//        }catch (IOException ioe){
//            Assert.fail("WatcherCacheImpl throws IOException: "+ioe.getMessage());
//        }
        cleanCacheFile();
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    public void makeCacheFile(){
        HashMap<Path, Long> cache = new HashMap<>();
        cache.put(Paths.get("/tmp/a.txt"), 1L );
        cache.put(Paths.get("/tmp/b.txt"), 2L );
        StringBuilder b = new StringBuilder();
        b.append("{").append("\n");
        String p; String l; int c = 1;
        for( Map.Entry<Path, Long> e: cache.entrySet() ){
            p = e.getKey().toFile().getAbsoluteFile().toString();
            l = e.getValue().toString();
            b.append(" \"").append(p).append("\": \"").append(l).append("\"");
            if( c++ != cache.size() )
                b.append(",");
            b.append("\n");
        }
        b.append("}");

        try {
            FileWriter fw = new FileWriter(Paths.get(PathsUtil.WATCHER_CACHE).toFile().getAbsolutePath());
            fw.append(b.toString());
            fw.flush();
            fw.close();
        }catch (IOException ioe){
            Assert.fail(ioe.getMessage());
        }
    }

    public void cleanCacheFile(){
        try {
            Files.delete(Paths.get(PathsUtil.WATCHER_CACHE));
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void makeAPPHOME(){
        try{
            PathsUtil.createApplicationHomeDirectory();
        }catch (IOException ioe){
            Assert.fail(ioe.getMessage());
        }
    }

    public Cache getInstance(){
        Cache w = null;
        try{
            w = Cache.getInstance();
        }catch (IOException ignored){}
        return w;
    }
}