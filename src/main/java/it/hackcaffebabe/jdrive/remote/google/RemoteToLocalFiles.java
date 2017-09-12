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
 * This data structure holds a map which use remote files as keys and local file
 * paths as values.
 */
public class RemoteToLocalFiles
{
    private static final Logger log = LogManager.getLogger();
    private static RemoteToLocalFiles instance;

    private static final ConcurrentHashMap<File, AccessiblePath> remoteToLocalFiles = new ConcurrentHashMap<>();

    /**
     * @return {@link it.hackcaffebabe.jdrive.remote.google.RemoteToLocalFiles}
     * the current instances.
     */
    public static RemoteToLocalFiles getInstance(){
        if( instance == null ){
            instance = new RemoteToLocalFiles();
        }
        return instance;
    }

    private RemoteToLocalFiles(){}

    /**
     * This method check if local path given can be accessed by other threads
     * @param localFilePath {@link java.nio.file.Path} the local file path to check
     * @return true if local file path can be accessed, false otherwise.
     */
    public synchronized boolean isAccessible( Path localFilePath ){
        Map.Entry<File, AccessiblePath> entry = lookupOf( localFilePath );
        return entry != null && entry.getValue().isAccessible();
    }

    /**
     * This method calls put function on each element of the given map.
     * @param map {@link java.util.Map} of remote-local files.
     */
    public synchronized void putAll( HashMap<File, Path> map ){
        map.forEach( this::put );
    }

    /**
     * Put a new key-value tuple with remoteFile as key and localFilePath as value.
     * New local file path put is assuming accessible from other threads.
     * @param remoteFile {@link File} remote file representation.
     * @param localFilePath {@link java.nio.file.Path} a local file path.
     */
    public synchronized void put( File remoteFile, Path localFilePath ) {
        put( remoteFile, localFilePath, true );
    }

    /**
     * Put a new key-value tuple with remoteFile as key, localFilePath as value
     * and with accessible flag.
     * @param remoteFile {@link File} remote file representation.
     * @param localFilePath {@link java.nio.file.Path} a local file path.
     * @param accessible true to make localFilePath accessible from other threads,
     *                   false otherwise.
     */
    public synchronized void put( File remoteFile, Path localFilePath, boolean accessible ){
        AccessiblePath accessiblePath = new AccessiblePath( localFilePath, accessible );
        remoteToLocalFiles.put( remoteFile, accessiblePath );
        log.debug("Added to map: [ "+remoteFile.getName()+", "+accessiblePath+" ]");
    }

    /**
     * Remove an entry associated with remoteFile as key.
     * @param remoteFile {@link File} the key to the map to remove.
     */
    public synchronized void remove( File remoteFile ) {
        AccessiblePath removedPath = remoteToLocalFiles.remove( remoteFile );
        log.debug("Removed from map: [ "+remoteFile.getName()+", " +removedPath+" ]");
    }

    /**
     * Retrieve the remote file associated with localFilePath given as argument.
     * This methods acts like a reverse get, because it's retrieve the key from
     * a specific value.
     * @param localFilePath {@link java.nio.file.Path} the local file path to
     *        retrieve the remote file associated.
     * @return {@link File} the remote file
     * @throws IOException if localFilePath is not associated with any remote
     *                     file.
     */
    public synchronized File get( Path localFilePath ) throws IOException {
        log.debug("Try to retrieve associated remote file form: "+localFilePath.toString());
        File file = getIfExists( localFilePath );
        if( file == null )
            throw new IOException(
                    "Remote file for local path "+localFilePath+" not found");
        log.debug("Remote file get: "+file.getName());
        return file;
    }

    /**
     * Retrieve the remote file associated with localFilePath given as argument
     * if exists, otherwise returns null. This methods works like get() but
     * return null instead throw an IOException.
     * @param localFilePath {@link java.nio.file.Path} the local file path to
     *        retrieve the remote file associated.
     * @return {@link File} the remote file.
     */
    public synchronized File getIfExists( Path localFilePath ) {
        Map.Entry<File, AccessiblePath> mapEntry = lookupOf( localFilePath );
        return mapEntry == null ? null : mapEntry.getKey();
    }

    // reverse lookup of map key by given his value
    private synchronized Map.Entry<File, AccessiblePath> lookupOf( Path localFilePath ) {
        return remoteToLocalFiles.entrySet()
            .stream()
            .filter( entry -> entry.getValue().getPath() != null &&
                     entry.getValue().getPath().equals(localFilePath) )
            .findAny()
            .orElse(null);
    }

//==============================================================================
//  INNER CLASS
//==============================================================================
    /**
     * Wrapper class that contains a local file path and a flag that tells other
     * threads if that path can be accessed.
     */
    public class AccessiblePath {
        private Path path;
        private boolean accessible;

        public AccessiblePath( Path path, boolean accessible ){
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
