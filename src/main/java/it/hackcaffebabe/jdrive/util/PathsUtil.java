package it.hackcaffebabe.jdrive.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import static it.hackcaffebabe.jdrive.Constants.APP_HOME;

/**
 * Utility class to working with Paths
 */
public class PathsUtil
{
    /** Set as System.getProperty("user.home") */
    public static final String USER_HOME = System.getProperty("user.home");
    /** Set as System.getProperty("file.separator") */
    public static final String SEP = System.getProperty("file.separator");

    /** Path to watcher cache file */
    public static final String WATCHER_CACHE = APP_HOME + SEP + "jwatch.cache";

    /**
     * This method check if APP_HOME exists, if not creates it.
     * @return {@link java.nio.file.Path} to home application.
     * @throws IOException if make new directory fail.
     */
    public static Path createApplicationHomeDirectory() throws IOException{
        return Files.createDirectories( Paths.get(APP_HOME) );
    }

    /**
     * This method create an empty cache file for WatcherCache class.
     * @return {@link java.nio.file.Path} of Watcher cache file.
     * @throws IOException if fail creating the file.
     */
    public static Path createWatcherCacheFile() throws IOException {
        Path cache = Paths.get(WATCHER_CACHE);
        if( !cache.toFile().exists() ){
            Files.createFile(cache);
        }
        return cache;
    }

    /**
     * This method checks if given path leads to a directory or not.
     * @param p {@link java.nio.file.Path} the path to check.
     * @return true if leads to a directory, false otherwise.
     */
    public static boolean isDirectory( Path p ){
        return Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * TODO add doc
     * @param file
     * @return
     */
    public static String getFileExtension(java.io.File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}
