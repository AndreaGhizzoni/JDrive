package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO add doc
 */
public class RemoteToLocalFiles
{
    private static final Logger log = LogManager.getLogger();
    private static RemoteToLocalFiles instance;

    private HashMap<File, Path> remoteToLocalFiles = new HashMap<>();

    public static RemoteToLocalFiles getInstance(){
        if( instance == null ){
            instance = new RemoteToLocalFiles();
        }
        return instance;
    }

    private RemoteToLocalFiles(){}

    public synchronized void put( File remoteFile, Path localFilePath ) {
        remoteToLocalFiles.put( remoteFile, localFilePath );
        log.debug("Added to map: [ "+remoteFile.getName()+", "
                +localFilePath+" ]");
    }

    public synchronized void putAll( HashMap<File, Path> map ){
        map.forEach( this::put );
    }

    public synchronized void remove( File remoteFile ) {
        Path removedPath = remoteToLocalFiles.remove( remoteFile );
        log.debug("Removed from map: [ "+remoteFile.getName()+", "
                +removedPath+" ]");
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
        Map.Entry<File, Path> mapEntry = remoteToLocalFiles.entrySet()
            .stream()
            .filter( entry -> entry.getValue() != null && entry.getValue().toAbsolutePath().equals(localFilePath) )
            .findAny()
            .orElse(null);

        return mapEntry == null ? null : mapEntry.getKey();
    }

}
