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
public class LocalToRemoteFiles
{
    private static final Logger log = LogManager.getLogger();
    private static LocalToRemoteFiles instance;

    private final ConcurrentHashMap<AccessiblePath, Optional<File>> localToRemote =
                                                     new ConcurrentHashMap<>();

    /**
     * TODO add doc
     * @return
     */
    public static LocalToRemoteFiles getInstance() {
        if( instance == null )
            instance = new LocalToRemoteFiles();
        return instance;
    }

    private LocalToRemoteFiles(){}

    public synchronized void put( Path path ) {
        put( path.toAbsolutePath() );
    }

    public synchronized void put( String path ) {
        put( path , null );
    }

    public synchronized void put( String path, File remoteFile ) {

    }

    public synchronized void putAll( Map<String, File> map ) {

    }

    public synchronized void putAll( List<String> paths ) {

    }

    public synchronized void put( String path, boolean accessible, File remote ){

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
