package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.cfg.Default;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Package class to provide IO to cache file.
 */
class WatcherCacheImpl implements WatcherCache{
    private static final Logger log = LogManager.getLogger(
        WatcherCacheImpl.class.getSimpleName()
    );

    private static WatcherCacheImpl instance;
    private final Path cacheFile = Paths.get(PathsUtil.WATCHER_CACHE);
    private HashMap<Path, Long> cache = new HashMap<>();

    /**
     * Return the instance of WatcherCache.
     * @return {@link WatcherCacheImpl } the WatcherCache
     * instance
     * @throws IOException if IO with cache fail.
     */
    public static WatcherCacheImpl getInstance() throws IOException {
        if( instance == null )
            instance = new WatcherCacheImpl();
        return instance;
    }

    /* if cache file doesn't exists, create ones and, in every case load it */
    private WatcherCacheImpl() throws IOException {
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

        StringBuilder sbKey, sbValue;
        char[] chars; int i; char tmp; boolean isKey;
        String line;
        boolean finish = false;
        while( !finish ){
            line = br.readLine();
            if( line == null ) {
                finish = true;
            }else{
                sbKey = new StringBuilder(); // clean buffers
                sbValue = new StringBuilder();
                // now check char-by-char the string line
                chars = line.toCharArray();
                i = 0; isKey = true;
                while ( i != chars.length ){
                    tmp = chars[i++];
                    if( tmp != '{' && tmp != '}' && tmp != ',' && tmp != ' '
                            && tmp != '"' ){
                        if( tmp == ':' ) isKey = false;
                        else{
                            if(isKey) sbKey.append(tmp);
                            else sbValue.append(tmp);
                        }
                    }
                }

                if( !sbKey.toString().isEmpty() )
                    put(Paths.get(sbKey.toString()), new Long(sbValue.toString()));
            }
        }
    }

    /* check given path */
    private void checkPathFile( Path p ) throws IllegalArgumentException{
        if( p == null )
            throw new IllegalArgumentException("Given path can not be null.");
    }

    /* check given last modify */
    private void checkLastModify( Long l ) throws IllegalArgumentException{
        if( l == null )
            throw new IllegalArgumentException("Given last modify can not be null.");
    }

    @Override
    public Set<Path> getCachedPaths(){ return this.cache.keySet(); }

    @Override
    public Long put( Path filePath,  Long lastModify ) throws IllegalArgumentException{
        checkPathFile(filePath);
        checkLastModify(lastModify);
        log.debug("Try to put: "+filePath+" : "+lastModify);
        return this.cache.put(filePath, lastModify);
    }

    @Override
    public Long get( Path filePath ) throws IllegalArgumentException{
        checkPathFile(filePath);
        return this.cache.get(filePath);
    }

    @Override
    public Long remove( Path filePath ) throws IllegalArgumentException{
        checkPathFile(filePath);
        log.debug("Try to remove key: "+filePath);
        return this.cache.remove(filePath);
    }

    @Override
    public boolean isWatched( Path filePath ) throws IllegalArgumentException{
        checkPathFile(filePath);
        return this.cache.containsKey( filePath );
    }

    @Override
    public void flush() throws IOException{
        FileWriter fw = new FileWriter(cacheFile.toFile());
        fw.append(this.toString());
        fw.flush();
        fw.close();
        log.debug("Cache flushed correctly");
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\n");
        String k, v; int c = 1;
        for( Map.Entry<Path, Long> e: this.cache.entrySet() ){
            k = e.getKey().toFile().getAbsoluteFile().toString();
            v = e.getValue().toString();
            sb.append(" \"").append(k).append("\": \"").append(v).append("\"");
            if( c++ != this.cache.size() )
                sb.append(",");
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
