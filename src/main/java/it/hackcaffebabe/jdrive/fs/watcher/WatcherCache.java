package it.hackcaffebabe.jdrive.fs.watcher;

import java.nio.file.Path;
import java.util.Set;

/**
 * Public interface to Cache access.
 */
public interface WatcherCache {

    /**
     * Returns all the paths in cache.
     * @return {@link java.util.Set} of cached {@link java.nio.file.Path} or null
     * if cache is empty.
     */
    public Set<Path> getCachedPaths();

    /**
     * Save a new {@link java.nio.file.Path} into the cache.
     * @param filePath {@link java.nio.file.Path} the file path.
     * @param lastModify {@link java.lang.Long} the last modify filed of file.
     * @return {@link java.lang.Long} the overwritten value, if key was already
     * present, otherwise null.
     */
    public Long put( Path filePath,  Long lastModify );

    /**
     * Return the last modify parameter of given {@link java.nio.file.Path}
     * @param filePath {@link java.nio.file.Path}
     * @return {@link java.lang.Long} the last modify value of given path.
     */
    public Long get( Path filePath );

    /**
     * Check if a {@link java.nio.file.Path} is present or not.
     * @param filePath {@link java.nio.file.Path} to check.
     * @return true if is present, false otherwise.
     */
    public boolean isWatched( Path filePath );

    /**
     * This method flush the current state of cache into APP_HOME/.jwatch.cache
     */
    public void flush();

}
