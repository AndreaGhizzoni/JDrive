package it.hackcaffebabe.jdrive.mapping;

import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.*;

/**
 * TODO add doc
 */
public class Mapper
{
    private static final Logger log = LogManager.getLogger();
    public static final boolean ENABLE_LOG = true;
    public static final boolean DISABLE_LOG = false;

    private HashMap<AccessiblePath, File> map = new HashMap<>();
    private PathSanitizer pathSanitizer = new PathSanitizer();
    private boolean logEnable = true;

    public Mapper(){ this( ENABLE_LOG); }

    public Mapper( boolean enableLog ) { this.logEnable = enableLog; }

    public File put( Path path ) { return put( path.toString() ); }

    public File put( String path ) { return put( path , null ); }

    public File put( String path, File remote ){ return put( path, true, remote ); }

    public File put( String path, boolean accessible, File remote ) {
        String sanitizedPath = pathSanitizer.sanitize( path );
        AccessiblePath accessiblePath = new AccessiblePath(
            sanitizedPath,
            accessible
        );

        String message;
        File previousFile = map.put( accessiblePath, remote );
        if( previousFile == null ) {
            message = "Put ok";
        }else{
            message = "Put overwritten previous value";
        }

        logIfEnabled(
            message,
            accessiblePath.getPath(),
            accessiblePath.isAccessible(),
            previousFile == null ? remote : previousFile
        );
        return previousFile;
    }

    public File get( Path path ) {
        return get( path.toString() );
    }

    public File get( String path ) {
        String sanitizedPath = pathSanitizer.sanitize( path );

        Optional<Map.Entry<AccessiblePath, File>> optional = map.entrySet()
            .stream()
            .filter( entry -> entry.getKey().getPath().equals(sanitizedPath) )
            .findAny();

        if( optional.isPresent() ){
            Map.Entry<AccessiblePath, File> entry = optional.get();
            logIfEnabled(
                "Get ok",
                entry.getKey().getPath(),
                entry.getKey().isAccessible(),
                entry.getValue()
            );
            return entry.getValue();
        }else{
            logIfEnabled(String.format(
                "Get remote file from path=%s not found", sanitizedPath)
            );
            return null;
        }
    }

    public Map<AccessiblePath, File> getImmutableMap() {
        return Collections.unmodifiableMap( map );
    }

    public void toggleAccessible( String path ) {
        String sanitizedPath = pathSanitizer.sanitize( path );
        Optional<Map.Entry<AccessiblePath, File>> optional = map.entrySet()
            .stream()
            .filter( entry -> entry.getKey().getPath().equals(sanitizedPath) )
            .findAny();

        if( optional.isPresent() ){
            Map.Entry<AccessiblePath, File> entryToOverwrite = optional.get();
            AccessiblePath accessiblePath = entryToOverwrite.getKey();
            String oldPath = accessiblePath.getPath();
            File remoteFile = entryToOverwrite.getValue();
            logIfEnabled(
                "Try to toggle accessible local file",
                oldPath,
                accessiblePath.isAccessible(),
                remoteFile
            );

            boolean newAccess = !accessiblePath.isAccessible();
            put( oldPath, newAccess, remoteFile );
        }
    }

    public String lookup( File remoteFile ) {
        Optional<AccessiblePath> optAccessiblePath = look( remoteFile );
        if( optAccessiblePath.isPresent() ){
            AccessiblePath accPath = optAccessiblePath.get();
            logIfEnabled(
                "Lookup",
                accPath.getPath(),
                accPath.isAccessible(),
                remoteFile
            );
            return pathSanitizer.restore( accPath.getPath() );
        }else{
            logIfEnabled(String.format(
                "Path associated with remote file %s not found",
                remoteFile == null ? "null" : remoteFile.getName()
            ));
            return null;
        }
    }

    private Optional<AccessiblePath> look( File remoteFile ){
        return map.entrySet()
            .stream()
            .filter( entry -> {
                File file = entry.getValue();
                return file != null && file.equals( remoteFile );
            })
            .map( Map.Entry::getKey )
            .findAny();
    }

    public File remove( String path ) {
        String sanitizedPath = pathSanitizer.sanitize( path );
        Optional<AccessiblePath> optional = map.keySet()
            .stream()
            .filter( accPath -> accPath.getPath().equals(sanitizedPath) )
            .findAny();

        if( optional.isPresent() ) {
            AccessiblePath accPath = optional.get();
            File remoteFileRemoved = map.remove( accPath );
            logIfEnabled(
                "Removed",
                sanitizedPath,
                accPath.isAccessible(),
                remoteFileRemoved
            );
            return remoteFileRemoved;
        }else{
            logIfEnabled(String.format(
                "Noting to remove: path=%s not found", sanitizedPath
            ));
            return null;
        }
    }

    public String remove( File remoteFile ) {
        Optional<AccessiblePath> optAccessiblePath = look( remoteFile );

        if( optAccessiblePath.isPresent() ) {
            String associatedPath = optAccessiblePath.get().getPath();
            remove( associatedPath );
            return pathSanitizer.restore( associatedPath );
        }else{
            logIfEnabled(String.format(
                "Noting to remove: remote file=%s not found",
                remoteFile == null ? "null" : remoteFile.getName()
            ));
            return null;
        }
    }

    public boolean isAccessible( Path path ) {
        return isAccessible( path.toString() );
    }

    public boolean isAccessible( String path ) {
        String sanitizedPath = pathSanitizer.sanitize( path );
        return map.keySet()
            .stream()
            .filter( accessiblePath -> accessiblePath.getPath().equals(sanitizedPath) )
            .findAny()
            .map( AccessiblePath::isAccessible )
            .orElse(true);
    }

    public boolean exists( Path path ) {
        return exists( path.toString() );
    }

    public boolean exists( String path ) {
        return map.containsKey( new AccessiblePath(path) );
    }

    private void logIfEnabled( String message ) {
        if( logEnable ) { log.debug( message ); }
    }

    private void logIfEnabled( String action,
                               String path,
                               boolean accessible,
                               File remoteFile ) {
        if( logEnable ) {
            log.debug(String.format(
                "%s [ path: %s, accessible: %s, remote: %s ]",
                action, path, accessible,
                remoteFile == null ? "null" : remoteFile.getName()
            ));
        }
    }
}
