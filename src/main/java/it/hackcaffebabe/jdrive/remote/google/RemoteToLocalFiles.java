package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This data structure holds a map which use remote files as keys and local file
 * paths as values.
 */
public class RemoteToLocalFiles
{
    private static final Logger log = LogManager.getLogger();
    private static RemoteToLocalFiles instance;

    private HashMap<File, Path> remoteToLocalFiles = new HashMap<>();

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
     * This method put a new remote-local file tuple into the map.
     * @param remoteFile {@link File} a remote file.
     * @param localFilePath {@link Path} a local file path.
     */
    public synchronized void put( File remoteFile, Path localFilePath ) {
        remoteToLocalFiles.put( remoteFile, localFilePath );
        log.debug("Added to map: [ "+remoteFile.getName()+", "
                +localFilePath+" ]");
    }

    /**
     * This method calls put function on each element of given map.
     * @param map {@link java.util.Map} of remote-local files.
     */
    public synchronized void putAll( HashMap<File, Path> map ){
        map.forEach( this::put );
    }

    /**
     * Remove an entry associated with remoteFile as key.
     * @param remoteFile {@link File} the key to the map to remove.
     */
    public synchronized void remove( File remoteFile ) {
        Path removedPath = remoteToLocalFiles.remove( remoteFile );
        log.debug("Removed from map: [ "+remoteFile.getName()+", "
                +removedPath+" ]");
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
        Map.Entry<File, Path> mapEntry = remoteToLocalFiles.entrySet()
            .stream()
            .filter( entry -> entry.getValue() != null && entry.getValue().toAbsolutePath().equals(localFilePath) )
            .findAny()
            .orElse(null);

        return mapEntry == null ? null : mapEntry.getKey();
    }
}
