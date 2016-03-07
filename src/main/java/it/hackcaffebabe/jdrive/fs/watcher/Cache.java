package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Package class to provide IO to cache file.
 */
class Cache
{
    private static final Logger log = LogManager.getLogger(
        Cache.class.getSimpleName()
    );

    private static Cache instance;
    private Path cacheFile;
    private HashMap<Path, Long> cache = new HashMap<>();

    // Path of last root directory used by Watcher
    private Path lastBasePath;

    /**
     * Return the instance of WatcherCache.
     * @return {@link Cache } the WatcherCache
     * instance
     * @throws IOException if IO with cache fail.
     */
    public static Cache getInstance() throws IOException {
        if( instance == null )
            instance = new Cache();
        return instance;
    }

    /* if cache file doesn't exists, create ones and, in every case load it */
    private Cache() throws IOException {
        cacheFile = PathsUtil.createWatcherCacheFile();
        log.debug("Cache file created/detected successfully.");
        loadCache();
    }

//==============================================================================
//  METHODS
//==============================================================================
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

                String key = sbKey.toString();
                String value = sbValue.toString();
                if( !key.isEmpty() ) {
                    if( key.startsWith("base") )
                        this.lastBasePath = Paths.get(value);
                    else
                        put(Paths.get(key), new Long(value));
                }
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

    public Long remove( Path filePath ) throws IllegalArgumentException{
        checkPathFile(filePath);
        log.debug("Try to remove key: "+filePath);
        return this.cache.remove(filePath);
    }

    public boolean isWatched( Path filePath ) throws IllegalArgumentException{
        checkPathFile(filePath);
        return this.cache.containsKey( filePath );
    }

    /**
     * TODO add doc
     * @return
     */
    public boolean isEmpty(){
        return this.lastBasePath == null && this.cache.isEmpty();
    }

    /**
     * TODO add doc
     * @throws IOException
     */
    public void flush() throws IOException{
        FileWriter fw = new FileWriter(cacheFile.toFile());
        fw.append(this.toString());
        fw.flush();
        fw.close();
        log.debug("Cache flushed correctly");
    }

//==============================================================================
//  SETTER
//==============================================================================
    /**
     * TODO add doc
     * @param filePath
     * @param lastModify
     * @return
     * @throws IllegalArgumentException
     */
    public Long put( Path filePath,  Long lastModify ) throws IllegalArgumentException{
        checkPathFile(filePath);
        checkLastModify(lastModify);
        log.debug("Try to put: "+filePath+" : "+lastModify);
        return this.cache.put(filePath, lastModify);
    }

    /**
     * TODO add doc
     * @param p
     * @return
     * @throws IllegalArgumentException
     */
    public Path putWatcherRoot( Path p ) throws IllegalArgumentException{
        checkPathFile(p);
        Path previous = this.lastBasePath;
        this.lastBasePath = p;
        return previous;
    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * TODO add doc
     * @return
     */
    public Set<Path> getCachedPaths(){ return this.cache.keySet(); }

    /**
     * TODO add doc
     * @return
     */
    public Path getWatcherRoot(){ return this.lastBasePath; }

    /**
     * TODO add doc
     * @param filePath
     * @return
     * @throws IllegalArgumentException
     */
    public Long get( Path filePath ) throws IllegalArgumentException{
        checkPathFile(filePath);
        return this.cache.get(filePath);
    }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{").append("\n");
        String k, v; /*int c = 1;*/
        for( Map.Entry<Path, Long> e: this.cache.entrySet() ){
            k = e.getKey().toFile().getAbsoluteFile().toString();
            v = e.getValue().toString();
            sb.append(" \"").append(k).append("\": \"").append(v).append("\"");
            //if( c++ != this.cache.size() )
            sb.append(",");
            sb.append("\n");
        }
        sb.append(" \"").append("base").append("\": \"")
                .append(this.lastBasePath.toAbsolutePath()).append("\"")
                .append("\n");
        sb.append("}");
        return sb.toString();
    }
}
