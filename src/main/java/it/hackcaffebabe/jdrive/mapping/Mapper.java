package it.hackcaffebabe.jdrive.mapping;

import com.google.api.services.drive.model.File;
import com.google.common.collect.Maps;
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
    private boolean logEnable = ENABLE_LOG;

    private HashMap<AccessiblePath, File> map = new HashMap<>();

    public Mapper(){ this(ENABLE_LOG); }

    public Mapper( boolean enableLog ) { this.logEnable = enableLog; }

    public Map<AccessiblePath, File> getImmutableMap() {
        return Collections.unmodifiableMap( map );
    }

    public File put( Path path ) { return put( path.toString() ); }

    public File put( String path ) { return put( path , null ); }

    public File put( String path, File remote ){ return put( path, true, remote ); }

    public File put( String path, boolean accessible, File remote ) {
        Map.Entry<AccessiblePath, File> newEntry = putting( path, accessible, remote );
        boolean overwrittenExistingFile = newEntry.getValue() != null;
        logIfEnabled(
            overwrittenExistingFile ? "Put overwritten previous value": "Put ok",
            newEntry.getKey().getPath(),
            newEntry.getKey().isAccessible(),
            overwrittenExistingFile ? newEntry.getValue() : remote
        );
        return newEntry.getValue();
    }

    Map.Entry<AccessiblePath, File> putting( String path,
                                             boolean accessible,
                                             File remote ) {
        AccessiblePath accessiblePath = new AccessiblePath( path, accessible );
        File overwrittenFile =  map.put( accessiblePath, remote );
        return Maps.immutableEntry( accessiblePath, overwrittenFile );
    }

    public File get( Path path ) { return get( path.toString() ); }

    public File get( String path ) {
        Optional<Map.Entry<AccessiblePath, File>> optional = getting( path );

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
                "Get remote file from path=%s not found", path )
            );
            return null;
        }
    }

    public Map.Entry<AccessiblePath, File> getFullEntry( String path ) {
        Optional<Map.Entry<AccessiblePath, File>> optional = getting( path );

        if( optional.isPresent() ){
            Map.Entry<AccessiblePath, File> entry = optional.get();
            logIfEnabled(
                "Get ok",
                entry.getKey().getPath(),
                entry.getKey().isAccessible(),
                entry.getValue()
            );
            return entry;
        }else{
            logIfEnabled(String.format(
                "Get remote file from path=%s not found", path )
            );
            return null;
        }
    }

    Optional<Map.Entry<AccessiblePath, File>> getting( String path ) {
        return map.entrySet()
            .stream()
            .filter( entry -> entry.getKey().getPath().equals(path) )
            .findAny();
    }

    public void toggleAccessible( String path ) {
        Optional<Map.Entry<AccessiblePath, File>> optional =  getting( path );

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
            return accPath.getPath();
        }else{
            logIfEnabled(String.format(
                "Path associated with remote file %s not found",
                remoteFile == null ? "null" : remoteFile.getName()
            ));
            return null;
        }
    }

    Optional<AccessiblePath> look( File remoteFile ){
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
        Optional<Map.Entry<AccessiblePath, File>> optional = removeFromKey( path );

        if( optional.isPresent() ) {
            AccessiblePath accessiblePathRemoved = optional.get().getKey();
            File remoteFileRemoved = optional.get().getValue();
            logIfEnabled(
                "Removed",
                accessiblePathRemoved.getPath(),
                accessiblePathRemoved.isAccessible(),
                remoteFileRemoved
            );
            return remoteFileRemoved;
        }else{
            logIfEnabled(String.format(
                "Noting to remove: path=%s not found", path
            ));
            return null;
        }
    }

    Optional<Map.Entry<AccessiblePath, File>> removeFromKey( String path ) {
        Optional<Map.Entry<AccessiblePath, File>> optional = getting( path );
        optional.ifPresent( entry -> map.remove(entry.getKey()) );
        return optional;
    }

    public String remove( File remoteFile ) {
        Optional<AccessiblePath> optional = removeFromValue( remoteFile );

        if( optional.isPresent() ) {
            AccessiblePath accPath = optional.get();
            logIfEnabled(
                "Removed",
                accPath.getPath(),
                accPath.isAccessible(),
                remoteFile
            );
            return accPath.getPath();
        }else{
            logIfEnabled(String.format(
                "Noting to remove: remote file=%s not found",
                remoteFile == null ? "null" : remoteFile.getName()
            ));
            return null;
        }
    }

    Optional<AccessiblePath> removeFromValue( File remoteFile ) {
        Optional<AccessiblePath> optAccessiblePath = look( remoteFile );
        optAccessiblePath.ifPresent( map::remove );
        return optAccessiblePath;
    }

    public boolean isAccessible( Path path ) {
        return isAccessible( path.toString() );
    }

    public boolean isAccessible( String path ) {
        return map.keySet()
            .stream()
            .filter( accPath -> accPath.getPath().equals(path) )
            .findAny()
            .map( AccessiblePath::isAccessible )
            .orElse(true);
    }

    public boolean exists( Path path ) { return exists( path.toString() ); }

    public boolean exists( String path ) {
        return map.containsKey( new AccessiblePath(path) );
    }

    private void logIfEnabled( String message ) {
        if( logEnable ) {
            log.debug( message );
        }
    }

    private void logIfEnabled( String action, String path,
                               boolean accessible, File remoteFile ) {
        if( logEnable ) {
            log.debug(String.format(
                "%s [ path: %s, accessible: %s, remote: %s ]",
                action, path, accessible,
                remoteFile == null ? "null" : remoteFile.getName()
            ));
        }
    }
}
