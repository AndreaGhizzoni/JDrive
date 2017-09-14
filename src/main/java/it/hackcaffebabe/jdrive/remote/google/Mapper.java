package it.hackcaffebabe.jdrive.remote.google;


import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO add doc
 */
public class Mapper
{
    private static final Logger log = LogManager.getLogger();
    private static Mapper instance;

    private final ConcurrentHashMap<AccessiblePath, Optional<File>> localToRemote =
                                                     new ConcurrentHashMap<>();

    /**
     * TODO add doc
     * @return
     */
    public static Mapper getInstance() {
        if( instance == null )
            instance = new Mapper();
        return instance;
    }

    private Mapper(){}

    public synchronized void put( Path path ) {
        put( path.toString() );
    }

    public synchronized void put( String path ) {
        put( path , Optional.ofNullable( null ) );
    }

    public synchronized void put( String path, Optional<File> remoteFile ) {
        put( path, true, remoteFile );
    }

    public synchronized void putAll( Map<String, Optional<File>> map ) {
        map.forEach( this::put );
    }

    public synchronized void putAll( List<String> paths ) {
        paths.forEach( this::put );
    }

    public synchronized void put( String path, boolean accessible, Optional<File> remote ){
        localToRemote.put( new AccessiblePath(path, accessible), remote );
        log.debug( String.format(
            "Put [ path: %s, accessible: %s, remote: %s ]",
            path, accessible, remote.map( f -> f.getName() ).orElse("null")
        ));
    }

    public synchronized File get( Path path ) throws IOException {
        return null;
    }

    public synchronized File get( String path ) throws IOException {
        return null;
    }

    public synchronized File getIfExists( String path ){
        return null;
    }

    public synchronized void remove( String path ) {

    }

    public synchronized void remove( File remoteFile ) {

    }

    public synchronized boolean isAccessible( String path ) {
        return false;
    }

    public synchronized boolean isAccessible( Path path ) {
        return false;
    }

//==============================================================================
//  INNER CLASS
//==============================================================================
    /**
     * Wrapper class that contains a file path and a flag that tells other
     * threads if that path can be accessed.
     */
    public class AccessiblePath {
        private String path;
        private boolean accessible;

        public AccessiblePath( String path, boolean accessible ){
            this.path = path;
            this.accessible = accessible;
        }

        public String getPath() { return this.path;  }

        public boolean isAccessible() { return this.accessible; }

        public String toString() {
            String format = "{path: %s, accessible: %s}";
            return String.format(
                format,
                this.path,
                String.valueOf(this.accessible)
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AccessiblePath that = (AccessiblePath) o;

            return path != null ? path.equals(that.path) : that.path == null;
        }

        @Override
        public int hashCode() {
            return path != null ? path.hashCode() : 0;
        }
    }
}
