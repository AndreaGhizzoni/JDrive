package it.hackcaffebabe.jdrive.util;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Default;
import it.hackcaffebabe.jdrive.cfg.Keys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to working with Paths
 */
public class PathsUtil
{
    /**
     * This method check if APP_HOME exists, if not creates it.
     * @throws IOException if make new directory fail.
     */
    public static void createApplicationHomeDirectory() throws IOException{
        Path p = java.nio.file.Paths.get(Default.APP_HOME);
        Files.createDirectories(p);
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
        Path p = java.nio.file.Paths.get(Default.APP_CGF_FILE);
        Files.createFile(p);
    }
}
