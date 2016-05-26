package it.hackcaffebabe.jdrive.util;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Default;
import it.hackcaffebabe.jdrive.cfg.Keys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to working with Paths
 */
public class PathsUtil
{
    // utility paths
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String SEP = System.getProperty("file.separator");

    // default application paths
    /** Path home application folder */
    public static final String APP_HOME = USER_HOME + SEP + ".jdrive";
    /** Path to application configuration file */
    public static final String APP_CGF_FILE = APP_HOME + SEP + "jdrive.conf";
    /** Path to watcher cache file */
    public static final String WATCHER_CACHE = APP_HOME + SEP + "jwatch.cache";
    /** Path to google Token Authentication */
    public static final File G_TOKEN = new File( APP_HOME + SEP + "t.json" );


    /**
     * This method check if APP_HOME exists, if not creates it.
     * @return {@link java.nio.file.Path} to home application.
     * @throws IOException if make new directory fail.
     */
    public static Path createApplicationHomeDirectory() throws IOException{
        return Files.createDirectories( Paths.get(APP_HOME) );
    }

    /**
     * This method get the value associated with Keys.WATCHED_DIR from Configuration
     * class and create it if not exists.
     * @return {@link java.nio.file.Path} scanned by Watcher class.
     * @throws IOException if make new directory fail.
     */
    public static Path createWatchedDirectory() throws IOException{
        Path wd = Paths.get( (String) Configurator.getInstance().get(Keys.WATCHED_DIR) );
        if( !wd.toFile().exists() )
            Files.createDirectories(wd);
        return wd;
    }

    /**
     * This method create an empty configuration file in Paths.APP_CGF_FILE.
     * @throws IOException if make new directory fail.
     */
    public static void createEmptyConfigurationFile() throws IOException {
        // create file ~/.jdrive/.jdrive.conf
        Path p = java.nio.file.Paths.get(APP_CGF_FILE);
        Files.createFile(p);
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
     * This method checks if given path leads to a directory or not.
     * @param f {@link java.io.File} the file object to check.
     * @return true if leads to a directory, false otherwise.
     */
    public static boolean isDirectory( File f ){
        Path p = Paths.get(f.getAbsolutePath());
        return PathsUtil.isDirectory(p);
    }
}
