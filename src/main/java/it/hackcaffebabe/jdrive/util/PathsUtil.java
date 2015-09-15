package it.hackcaffebabe.jdrive.util;

import it.hackcaffebabe.jdrive.cfg.Default;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class to working with Paths
 */
public class PathsUtil
{
    /**
     * This method check if APP_HOME exists, if not creates it.
     * @throws IOException if make new directory fail.
     */
    public static void buildWorkingDirectory() throws IOException{
        Path p = java.nio.file.Paths.get(Default.APP_CGF_FILE);
        Files.createDirectories(p);
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
