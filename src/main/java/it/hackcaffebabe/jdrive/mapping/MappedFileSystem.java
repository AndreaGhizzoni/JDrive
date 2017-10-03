package it.hackcaffebabe.jdrive.mapping;

import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * TODO add doc
 */
public class MappedFileSystem
{
    private static final Logger log = LogManager.getLogger();
    private static MappedFileSystem instance;

    private Mapper map = new Mapper( Mapper.DISABLE_LOG );

    public static synchronized MappedFileSystem getInstance() {
        if( instance == null )
            instance = new MappedFileSystem();
        return instance;
    }

    private MappedFileSystem() {
        log.debug("Mapped File System creation.");
    }

    public synchronized File put( Path localPath ) {
        return put( localPath, null );
    }

    public synchronized File put( Path localPath, File remoteFile ) {
        return put( localPath.toString(), remoteFile );
    }

    public synchronized File put( String localPathString, File remoteFile ) {
        return put( localPathString, remoteFile, true );
    }

    public synchronized File put( Path localPath, File remoteFile, boolean accessible ){
        return put( localPath.toString(), remoteFile, accessible );
    }

    public synchronized File put( String localPathString, File remoteFile,
                                  boolean accessible ){
        Map.Entry<AccessiblePath, File> newEntry = map.putting( localPathString, accessible, remoteFile );
        boolean overwrittenExistingRemoteFile = newEntry.getValue() != null;
        log(
            overwrittenExistingRemoteFile ? "OVERWRITTEN": "STORED",
            newEntry.getKey().getPath(),
            newEntry.getKey().isAccessible(),
            overwrittenExistingRemoteFile ? newEntry.getValue() : remoteFile
        );
        return newEntry.getValue();
    }

    public synchronized File get( Path localPath ) {
        return get( localPath.toString() );
    }

    public synchronized File get( String localPathString ){
        Optional<Map.Entry<AccessiblePath, File>> optional = map.getting( localPathString );

        if( optional.isPresent() ){
            Map.Entry<AccessiblePath, File> entry = optional.get();
            log(
                "READ",
                entry.getKey().getPath(),
                entry.getKey().isAccessible(),
                entry.getValue()
            );
            return entry.getValue();
        }else{
            log(String.format(
                "READ FAIL %s not found", localPathString )
            );
            return null;
        }
    }

    public synchronized void toggleAccessible( Path localPath ){
        toggleAccessible( localPath.toString() );
    }

    public synchronized void toggleAccessible( String localPathString ) {
        Optional<Map.Entry<AccessiblePath, File>> optional =  map.getting( localPathString );

        if( optional.isPresent() ){
            Map.Entry<AccessiblePath, File> entryToOverwrite = optional.get();
            AccessiblePath accessiblePath = entryToOverwrite.getKey();
            String oldPath = accessiblePath.getPath();
            File remoteFile = entryToOverwrite.getValue();
            log(
                "TOGGLE",
                oldPath,
                accessiblePath.isAccessible(),
                remoteFile
            );

            boolean newAccess = !accessiblePath.isAccessible();
            put( oldPath, remoteFile, newAccess );
        }
    }

    public synchronized String lookup( File remoteFile ) {
        Optional<AccessiblePath> optAccessiblePath = map.look( remoteFile );
        if( optAccessiblePath.isPresent() ){
            AccessiblePath accPath = optAccessiblePath.get();
            log(
                "LOOKUP",
                accPath.getPath(),
                accPath.isAccessible(),
                remoteFile
            );
            return accPath.getPath();
        }else{
            log(String.format(
                "LOOKUP FAIL: %s not found",
                remoteFile == null ? "null" : remoteFile.getName()
            ));
            return null;
        }
    }

    public synchronized File remove( Path path ) {
        Optional<Map.Entry<AccessiblePath, File>> optional = map.removeFromKey( path.toString() );

        if( optional.isPresent() ) {
            AccessiblePath accessiblePathRemoved = optional.get().getKey();
            File remoteFileRemoved = optional.get().getValue();
            log(
                "REMOVED",
                accessiblePathRemoved.getPath(),
                accessiblePathRemoved.isAccessible(),
                remoteFileRemoved
            );
            return remoteFileRemoved;
        }else{
            log(String.format("REMOVE FAIL: %s not found", path));
            return null;
        }
    }

    public synchronized String remove( File remoteFile ) {
        Optional<AccessiblePath> optional = map.removeFromValue( remoteFile );

        if( optional.isPresent() ) {
            AccessiblePath accPath = optional.get();
            log(
                "REMOVED",
                accPath.getPath(),
                accPath.isAccessible(),
                remoteFile
            );
            return accPath.getPath();
        }else{
            log(String.format(
                "REMOVE FAIL: %s not found",
                remoteFile == null ? "null" : remoteFile.getName()
            ));
            return null;
        }
    }

    public synchronized boolean isAccessible( Path localPath ) {
        return map.isAccessible( localPath );
    }

    public synchronized boolean exists( Path localPath ) {
        return map.exists( localPath );
    }

    private void log( String action, String path,
                      boolean accessible, File remoteFile ) {
        log.debug(String.format(
            "%s [ path: %s, accessible: %s, remote: %s ]",
            action, path, accessible,
            remoteFile == null ? "null" : remoteFile.getName()
        ));
    }

    private void log( String message ) { log.debug( message ); }
}
