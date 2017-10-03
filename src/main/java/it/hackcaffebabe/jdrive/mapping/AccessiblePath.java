package it.hackcaffebabe.jdrive.mapping;

/**
 * Wrapper class that contains a file path and a flag that indicates if path
 * is accessible or not. Check if path is accessible or not is delegate to the
 * user of this class.
 */
public class AccessiblePath
{
    private String path;
    private boolean accessible;

    private PathSanitizer sanitizer = new PathSanitizer();

    /**
     * Instance an object with given path as accessible.
     * @param path {@link java.nio.file.Path} a file path.
     */
    public AccessiblePath( String path ){ this( path, true ); }

    /**
     * Instance an object with given path and accessible flag.
     * @param path {@link java.nio.file.Path} a file path.
     * @param accessible flag to let path accessible.
     */
    public AccessiblePath( String path, boolean accessible ){
        this.path = sanitizer.sanitize( path );
        this.accessible = accessible;
    }

    /**
     * Return the file path. Checking if this path is accessible is
     * responsibility of the caller.
     * @return {@link java.lang.String} the file path.
     */
    public String getPath() { return sanitizer.restore( this.path ); }

    /**
     * Return whenever the file path is accessible or not.
     * @return true if file path is accessible, false otherwise.
     */
    public boolean isAccessible() { return this.accessible; }

    @Override
    public String toString() {
        return String.format(
            "{path: %s, accessible: %s}",
            getPath(),
            String.valueOf( isAccessible() )
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
    public int hashCode() { return path != null ? path.hashCode() : 0; }
}
