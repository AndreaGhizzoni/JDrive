package it.hackcaffebabe.jdrive.mapping;

/**
 * Wrapper class that contains a file path and a flag that tells other
 * threads if that path can be accessed.
 */
public class AccessiblePath {
    private String path;
    private boolean accessible;

    AccessiblePath(String path ){
        this( path, true );
    }

    AccessiblePath(String path, boolean accessible ){
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
