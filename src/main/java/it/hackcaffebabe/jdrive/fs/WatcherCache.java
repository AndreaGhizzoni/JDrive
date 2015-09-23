package it.hackcaffebabe.jdrive.fs;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.hackcaffebabe.jdrive.cfg.Default;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Package class to provide IO to cache file.
 */
class WatcherCache {
    private static final Logger log = LogManager.getLogger(
        WatcherCache.class.getSimpleName()
    );

    private static WatcherCache instance;
    private final Path cacheFile = Paths.get(Default.WATCHER_CACHE);
    private HashMap<Path, Long> cache = new HashMap<>();

    /**
     * Return the instance of WatcherCache.
     * @return {@link it.hackcaffebabe.jdrive.fs.WatcherCache } the WatcherCache
     * instance
     * @throws IOException if IO with cache fail.
     */
    public static WatcherCache getInstnace() throws IOException {
        if( instance == null )
            instance = new WatcherCache();
        return instance;
    }

    /* if cache file doesn't exists, create ones and, in every case load it */
    private WatcherCache() throws IOException {
        if( !cacheFile.toFile().exists() ) {
            Files.createFile(cacheFile);
            log.debug("Cache file created successfully.");
        }
        loadCache();
    }

    /* read the cache file and load data from it */
    private void loadCache() throws IOException{
        log.debug("Try to load cache file.");
        BufferedReader br = new BufferedReader( new FileReader(cacheFile.toFile()) );
        StringBuilder b = new StringBuilder();

        boolean finish = false; String line;
        while( !finish ){
            line = br.readLine();
            if( line == null )
                finish = true;
            else
                b.append(line);
        }

        String json = b.toString();
        if( !json.isEmpty() ) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            this.cache = new Gson().fromJson(json, type);
            log.debug("cache: "+json);
        }else{
            log.debug("cache empty.");
        }
    }

    /**
     * Returns all the paths in cache.
     * @return {@link java.util.Set} of cached {@link java.nio.file.Path} or null
     * if cache is empty.
     */
    public Set<Path> getCachedPaths(){
        return this.cache.keySet();
    }

    /**
     * Save a new {@link java.nio.file.Path} into the cache.
     * @param filePath {@link java.nio.file.Path} the file path.
     * @param lastModify {@link java.lang.Long} the last modify filed of file.
     * @return {@link java.lang.Long} the overwritten value, if key was already
     * present, otherwise null.
     */
    public Long put( Path filePath,  Long lastModify ){
        return this.cache.put(filePath, lastModify);
    }

    /**
     * Return the last modify parameter of given {@link java.nio.file.Path}
     * @param filePath {@link java.nio.file.Path}
     * @return {@link java.lang.Long} the last modify value of given path.
     */
    public Long get( Path filePath ){
        return this.cache.get(filePath);
    }

    /**
     * Check if a {@link java.nio.file.Path} is present or not.
     * @param filePath {@link java.nio.file.Path} to check.
     * @return true if is present, false otherwise.
     */
    public boolean isWatched( Path filePath ){
        return this.cache.containsKey( filePath );
    }

    /**
     * This method flush the current state of cache into APP_HOME/.jwatch.cache
     */
    public void flush(){
        try {
            FileWriter fw = new FileWriter(cacheFile.toFile());
            fw.append(this.toString());
            fw.flush();
            fw.close();
            log.debug("Cache flushed correctly");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String toString(){
        Gson g = new Gson();
        return g.toJson(this.cache);
//        StringBuilder b = new StringBuilder();
//        b.append("{").append("\n");
//        String p; String l; int c = 1;
//        for( Map.Entry<Path, Long> e: this.cache.entrySet() ){
//            p = e.getKey().toFile().getAbsoluteFile().toString();
//            l = e.getValue().toString();
//            b.append(" \"").append(p).append("\": \"").append(l).append("\"");
//            if( c++ != this.cache.size() )
//                b.append(",");
//            b.append("\n");
//        }
//        b.append("}");
//        return b.toString();
    }
}
