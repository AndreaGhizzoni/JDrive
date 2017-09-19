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
        log.debug("Mapped File System request instance...");
        if( instance == null )
            instance = new MappedFileSystem();
        return instance;
    }

    private MappedFileSystem() {}

    public synchronized void put( Path localPath ) {
        log.debug("Try to store path="+localPath+" with no remote file");
        map.put( localPath );
    }

    public synchronized void put( Path localPath, File remoteFile ) {
        log.debug(String.format(
            "Try to store path=%s with remote file=%s",
            localPath.toString(), remoteFile == null ? "null" : remoteFile.getName()
        ));
        map.put( localPath.toString(), remoteFile );
    }

    public synchronized File get( Path localPath ) {
        log.debug("Try to read remote file from path="+localPath);
        return map.get( localPath );
    }

    public synchronized String lookup( File remoteFile ) {
        log.debug("Try to read local path from remote file="+remoteFile.getName());
        return map.lookup( remoteFile );
    }

    public synchronized File remove( Path path ) {
        log.debug("Try to delete path="+path);
        return map.remove( path.toString() );
    }

    public synchronized String remove( File remoteFile ) {
        log.debug(String.format(
            "Try to delete path from remote file=%s",
            remoteFile == null ? "null" : remoteFile.getName()
        ));
        return map.remove( remoteFile );
    }

    public synchronized boolean isAccessible( Path localPath ) {
        log.debug("Try to read if path="+localPath+" is accessible");
        return map.isAccessible( localPath );
    }
}
