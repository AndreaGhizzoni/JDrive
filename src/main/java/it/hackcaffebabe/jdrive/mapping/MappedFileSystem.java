package it.hackcaffebabe.jdrive.mapping;

import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 * TODO add doc
 */
public class MappedFileSystem
{
    private static final Logger log = LogManager.getLogger();
    private static MappedFileSystem instance;

    private Mapper map = new Mapper();

    public static synchronized MappedFileSystem getInstance() {
        if( instance == null )
            instance = new MappedFileSystem();
        return instance;
    }

    private MappedFileSystem() {
        log.debug("Mapped File System creation.");
    }

    public synchronized void put( Path localPath ) {
        put( localPath, null );
    }

    public synchronized void put( Path localPath, File remoteFile ) {
        put( localPath.toString(), remoteFile );
    }

    public synchronized void put( String localPathString, File remoteFile ) {
        put( localPathString, remoteFile, true );
    }

    public synchronized void put( Path localPath, File remoteFile, boolean accessible ){
        put( localPath.toString(), remoteFile, accessible );
    }

    public synchronized void put( String localPathString, File remoteFile, boolean accessible ){
        map.put( localPathString, accessible, remoteFile );
    }

    public synchronized void toggleAccessible( Path localPath ){
        toggleAccessible( localPath.toString() );
    }

    public synchronized void toggleAccessible( String localPathString ) {
        map.toggleAccessible( localPathString );
    }

    public synchronized File get( Path localPath ) {
        return get( localPath.toString() );
    }

    public synchronized File get( String localPathString ){
        return map.get( localPathString );
    }

    public synchronized String lookup( File remoteFile ) {
        return map.lookup( remoteFile );
    }

    public synchronized File remove( Path path ) {
        return map.remove( path.toString() );
    }

    public synchronized String remove( File remoteFile ) {
        return map.remove( remoteFile );
    }

    public synchronized boolean isAccessible( Path localPath ) {
        return map.isAccessible( localPath );
    }

    public synchronized boolean exists( Path localPath ) {
        return map.exists( localPath );
    }
}
