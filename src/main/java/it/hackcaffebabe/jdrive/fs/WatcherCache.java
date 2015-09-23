package it.hackcaffebabe.jdrive.fs;

import it.hackcaffebabe.jdrive.cfg.Default;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public static WatcherCache getInstnace() throws IOException {
        if( instance == null )
            instance = new WatcherCache();
        return instance;
    }

    private WatcherCache() throws IOException {
        if( !cacheFile.toFile().exists() ) {
            Files.createFile(cacheFile);
            log.debug("Cache file created successfully.");
        }
    }

    public Set<Path> getCachedPaths(){
        return this.cache.keySet();
    }

    public boolean put( Path filePath,  Long lastModify ){
        Long alreadyExists = this.cache.put(filePath, lastModify);
        return alreadyExists != null; // if key already exists return true.
    }

    public Long get( Path filePath ){
        return this.cache.get(filePath);
    }

    public boolean isWatched( Path filePath ){
        return this.cache.containsKey( filePath );
    }

    // used when watched will exit
    // delete the old cache file and print the new one
    public void flush(){
        try {
            FileWriter fw = new FileWriter(cacheFile.toFile());
            fw.append(this.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append("{").append("\n");
        String p; String l; int c = 1;
        for( Map.Entry<Path, Long> e: this.cache.entrySet() ){
            p = e.getKey().toFile().getAbsoluteFile().toString();
            l = e.getValue().toString();
            b.append(" \"").append(p).append("\": \"").append(l).append("\"");

            if( c++ != this.cache.size() )
                b.append(",");
            b.append("\n");
        }
        b.append("}");
        return b.toString();
    }
}
