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
public class Mapper
{
    private static final Logger log = LogManager.getLogger();
    private static Mapper instance;

    private final ConcurrentHashMap<AccessiblePath, Optional<File>> localToRemote =
                                                     new ConcurrentHashMap<>();

    /**
     * TODO add doc
     * @return
     */
    public static Mapper getInstance() {
        if( instance == null )
            instance = new Mapper();
        return instance;
    }

    private Mapper(){}

    public synchronized void put( Path path ) {
        put( path.toString() );
    }

    public synchronized void put( String path ) {
        put( path , null );
    }

    public synchronized void put( String path, File remote ){
        put( path, true, Optional.ofNullable(remote) );
    }

    public synchronized void putAll( Map<String, File> map ) {
        map.forEach( this::put );
    }

    public synchronized void putAll( List<String> paths ) {
        paths.forEach( this::put );
    }

    public synchronized void put( String path, boolean accessible, Optional<File> remote ){
        localToRemote.put( new AccessiblePath(path, accessible), remote );
        logEntry("Put", path, accessible, remote);
    }

    public synchronized File get( Path path ) {
        return get( path.toString() );
    }

    public synchronized File get( String path ) {
        AccessiblePath accessiblePath = new AccessiblePath( path, true );
        Optional<File> optFile = localToRemote.get( accessiblePath );

        logEntry("Get", accessiblePath, optFile );
        return optFile == null ? null : optFile.get();
    }

    public synchronized String lookup( File remote ){
        Optional<AccessiblePath> optAccessiblePath = look( remote );

        logEntry(
            "Lookup",
            optAccessiblePath.map( accPath -> accPath.getPath() ).orElse("null"),
            optAccessiblePath.map( accPath -> accPath.isAccessible() ).orElse(false),
            Optional.ofNullable(remote)
        );
        return optAccessiblePath.isPresent() ? optAccessiblePath.get().getPath() : null;
    }

    private synchronized Optional<AccessiblePath> look( File remote ){
        return localToRemote.entrySet()
            .stream()
            .filter( entry -> {
                Optional<File> optFile = entry.getValue();
                return optFile.isPresent() && optFile.get().equals( remote );
            })
            .map( entry -> entry.getKey() )
            .findAny();
    }

    public synchronized void remove( String path ) {
        Optional<File> remoteFile = localToRemote.remove(
            new AccessiblePath( path, true )
        );
        logEntry("Removed", path, true, remoteFile);
    }

    public synchronized void remove( File remoteFile ) {
        Optional<AccessiblePath> optAccessiblePath = look( remoteFile );

        if( optAccessiblePath.isPresent() ) {
            remove( optAccessiblePath.get().getPath() );
        }else{
            log.debug(String.format(
                "Noting to do: remote file %s not found with any associated key.",
                remoteFile.getName()
            ));
        }
    }

    public synchronized boolean isAccessible( Path path ) {
        return isAccessible( path.toString() );
    }

    public synchronized boolean isAccessible( String path ) {
        return localToRemote.keySet()
            .stream()
            .filter( accessiblePath -> accessiblePath.getPath().equals(path) )
            .findAny()
            .map( accessiblePath -> accessiblePath.isAccessible() )
            .orElse(false);
    }

    private synchronized void logEntry( String action, AccessiblePath accessiblePath,
                                        Optional<File> optfile ) {
        logEntry(
            action, accessiblePath.getPath(), accessiblePath.isAccessible(),
            optfile
        );
    }

    private synchronized void logEntry( String action, String path,
                                        boolean accessible, Optional<File> file ) {
        log.debug( String.format(
            "%s [ path: %s, accessible: %s, remote: %s ]",
            action, path, accessible, file.map( f -> f.getName() ).orElse("null") )
        );
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
