package it.hackcaffebabe.jdrive.remote.google;


import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO add doc
 */
public class MapperV2
{
    private static final Logger log = LogManager.getLogger();

    private HashMap<AccessiblePath, File> map = new HashMap<>();

    public MapperV2(){}

    public void put( Path path ) {
        put( path.toString() );
    }

    public void put( String path ) {
        put( path , null );
    }

    public void put( String path, File remote ){
        put( path, true, remote );
    }

    public void putAll( Map<String, File> map ) {
        map.forEach( this::put );
    }

    public void putAll( List<String> paths ) {
        paths.forEach( this::put );
    }

    public void put( String path, boolean accessible, File remote ){
        AccessiblePath accessiblePath = new AccessiblePath( path, accessible );
        logEntry("Try to Put", accessiblePath, remote);
        File previousFile = map.put( accessiblePath, remote );
        if( previousFile == null ) {
            log.debug("Put ok.");
        }else{
            log.debug(String.format(
                "Put overwritten previous value: %s", previousFile.getName()
            ));
        }
    }

    public File get( Path path ) {
        return get( path.toString() );
    }

    public File get( String path ) {
        log.debug("Try to get remote file associated to path="+path);
        AccessiblePath accessiblePath = new AccessiblePath( path );
        File file = map.get( accessiblePath );
        if( file == null ) {
            log.debug(String.format("Remote file from path=%s not found", path));
            return null;
        }else {
            logEntry("Get ok", accessiblePath, file);
            return file;
        }
    }

    public String lookup( File remoteFile ){
        log.debug(String.format(
            "Try to lookup path associated with remote file: %s",
            remoteFile == null ? "null" : remoteFile.getName()
        ));
        Optional<AccessiblePath> optAccessiblePath = look( remoteFile );

        if( optAccessiblePath.isPresent() ){
            logEntry(
                "Lookup",
                optAccessiblePath.get(),
                remoteFile
            );
            return optAccessiblePath.get().getPath();
        }else{
            log.debug(String.format(
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
        log.debug("Try to remove remote file associated with path: "+path);
        File remoteFileRemoved = map.remove( new AccessiblePath(path) );

        if( remoteFileRemoved == null ){
            log.debug("Remote file associated with path="+path+" not found");
            return null;
        }else{
            logEntry("Removed", path, true, remoteFileRemoved);
            return remoteFileRemoved;
        }
    }

    public String remove( File remoteFile ) {
        log.debug(String.format(
            "Try to remove path from remote file: %s",
            remoteFile == null ? "null" : remoteFile.getName()
        ));
        Optional<AccessiblePath> optAccessiblePath = look( remoteFile );

        if( optAccessiblePath.isPresent() ) {
            String associatedPath = optAccessiblePath.get().getPath();
            remove( associatedPath );
            return associatedPath;
        }else{
            log.debug(String.format(
                "Noting to do: remote file %s not found with any associated path.",
                remoteFile == null ? "null" : remoteFile.getName()
            ));
            return null;
        }
    }

    public boolean isAccessible( Path path ) {
        return isAccessible( path.toString() );
    }

    public boolean isAccessible( String path ) {
        return map.keySet()
            .stream()
            .filter( accessiblePath -> accessiblePath.getPath().equals(path) )
            .findAny()
            .map( AccessiblePath::isAccessible )
            .orElse(false);
    }

    private void logEntry( String action, AccessiblePath accessiblePath,
                                        File remoteFile ) {
        logEntry(
            action, accessiblePath.getPath(), accessiblePath.isAccessible(),
            remoteFile
        );
    }

    private void logEntry( String action, String path, boolean accessible,
                           File remoteFile ) {
        log.debug( String.format(
            "%s [ path: %s, accessible: %s, remote: %s ]",
            action, path, accessible,
            remoteFile == null ? "null" : remoteFile.getName()
        ));
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

        AccessiblePath( String path ){
            this( path, true );
        }

        AccessiblePath( String path, boolean accessible ){
            this.path = path;
            this.accessible = accessible;
        }

        public String getPath() { return this.path;  }

        boolean isAccessible() { return this.accessible; }

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
