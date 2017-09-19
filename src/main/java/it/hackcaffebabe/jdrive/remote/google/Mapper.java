package it.hackcaffebabe.jdrive.remote.google;


import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.util.PathsUtil;
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
public class Mapper
{
    private static final Logger log = LogManager.getLogger();

    private HashMap<AccessiblePath, File> map = new HashMap<>();
    private PathSanitizer pathSanitizer = new PathSanitizer();

    public Mapper(){}

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
        String sanitizedPath = pathSanitizer.sanitize( path );
        AccessiblePath accessiblePath = new AccessiblePath(
            sanitizedPath,
            accessible
        );
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
        String sanitizedPath = pathSanitizer.sanitize( path );
        log.debug("Try to get remote file associated to path="+sanitizedPath);
        AccessiblePath accessiblePath = new AccessiblePath( sanitizedPath );
        File file = map.get( accessiblePath );
        if( file == null ) {
            log.debug(String.format("Remote file from path=%s not found", sanitizedPath));
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
            return pathSanitizer.restore( optAccessiblePath.get().getPath() );
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
        String sanitizedPath = pathSanitizer.sanitize( path );
        log.debug("Try to remove remote file associated with path: "+sanitizedPath);
        File remoteFileRemoved = map.remove( new AccessiblePath(sanitizedPath) );

        if( remoteFileRemoved == null ){
            log.debug("Remote file associated with path="+sanitizedPath+" not found");
            return null;
        }else{
            logEntry("Removed", sanitizedPath, true, remoteFileRemoved);
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
            return pathSanitizer.restore(associatedPath);
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
        String sanitizedPath = pathSanitizer.sanitize( path );
        return map.keySet()
            .stream()
            .filter( accessiblePath -> accessiblePath.getPath().equals(sanitizedPath) )
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

    /**
     * TODO add doc
     */
    public class PathSanitizer implements Sanitizer {
        final String base = PathsUtil.USER_HOME+PathsUtil.SEP;
        final Logger log = LogManager.getLogger();

        @Override
        public String sanitize(String toSanitize) {
            log.debug("Try to sanitize path="+toSanitize);
            if( toSanitize.startsWith(base) ){
                String sanitized = toSanitize.replaceFirst(base, "");
                log.debug("Path sanitized="+sanitized);
                return sanitized;
            }else{
                log.debug("Noting to sanitize: path not start with base");
                return toSanitize;
            }
        }

        public String restore(String sanitized) {
            log.debug("Try to restore path="+sanitized);
            String restored = base+sanitized;
            log.debug("Restored path="+restored);
            return restored;
        }
    }
}
