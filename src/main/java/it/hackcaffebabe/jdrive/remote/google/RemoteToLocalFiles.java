package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TODO add doc
 */
public class RemoteToLocalFiles
{
    private static final Logger log = LogManager.getLogger();
    private static RemoteToLocalFiles instance;

    private static final ConcurrentHashMap<File, AccessiblePath> remoteToLocalFiles = new ConcurrentHashMap<>();

    public static RemoteToLocalFiles getInstance(){
        if( instance == null ){
            instance = new RemoteToLocalFiles();
        }
        return instance;
    }

    private RemoteToLocalFiles(){}

    public synchronized boolean isAccessible( Path localFilePath ){
        Map.Entry<File, AccessiblePath> entry = lookupOf( localFilePath );
        return entry != null && entry.getValue().isAccessible();
    }

    public synchronized void put( File remoteFile, Path localFilePath ) {
        put( remoteFile, localFilePath, true );
    }

    public synchronized void putAll( HashMap<File, Path> map ){
        for( Map.Entry<File, Path> entry: map.entrySet() ){
            put( entry.getKey(), entry.getValue(), true );
        }
    }

    public synchronized void put( File remoteFile, Path localFilePath, boolean accessible ){
        AccessiblePath accessiblePath = new AccessiblePath( localFilePath, accessible );
        remoteToLocalFiles.put( remoteFile, accessiblePath );
        log.debug("Added to map: [ "+remoteFile.getName()+", "+accessiblePath+" ]");
    }

    public synchronized void remove( File remoteFile ) {
        AccessiblePath removedPath = remoteToLocalFiles.remove( remoteFile );
        log.debug("Removed from map: [ "+remoteFile.getName()+", " +removedPath+" ]");
    }

    public synchronized File get( Path localFilePath ) throws IOException {
        log.debug("Try to retrieve associated remote file form: "+localFilePath.toString());
        File file = getIfExists( localFilePath );
        if( file == null )
            throw new IOException(
                    "Remote file for local path "+localFilePath+" not found");
        log.debug("Remote file get: "+file.getName());
        return file;
    }

    public synchronized File getIfExists( Path localFilePath ) {
        Map.Entry<File, AccessiblePath> mapEntry = lookupOf( localFilePath );
        return mapEntry == null ? null : mapEntry.getKey();
    }

    private synchronized Map.Entry<File, AccessiblePath> lookupOf( Path localFilePath ) {
        return remoteToLocalFiles.entrySet()
            .stream()
            .filter( entry -> entry.getValue().getPath() != null && entry.getValue().getPath().equals(localFilePath) )
            .findAny()
            .orElse(null);
    }

//==============================================================================
//  INNER CLASS
//==============================================================================
    public class AccessiblePath {
        private Path path;
        private boolean accessible;

        public AccessiblePath(Path path, boolean accessible ){
            this.path = path;
            this.accessible = accessible;
        }

        public Path getPath() { return this.path;  }

        public boolean isAccessible() { return this.accessible; }

        public String toString() {
            String format = "{path: %s, accessible: %s}";
            return String.format(
                format,
                this.path,
                String.valueOf(this.accessible)
            );
        }
    }
}
