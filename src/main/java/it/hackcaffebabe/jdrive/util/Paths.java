package it.hackcaffebabe.jdrive.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class to provide the basic path of the program
 */
public class Paths
{
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String SEP = System.getProperty("file.separator");
    public static final String USER_HOME_AND_SEP = USER_HOME+SEP;

    public static final String PATH_APP = USER_HOME_AND_SEP+".jdrive";
    public static final String PATH_CFG = PATH_APP+SEP+"jdrive.conf";

    /**
     * This method check if PATH_APP exists, if not creates it.
     * @throws IOException if make new directory fail.
     */
    public static void buildWorkingDirectory() throws IOException{
        Path p = java.nio.file.Paths.get(PATH_APP);
        Files.createDirectories(p);
    }

    /**
     * This method create an empty configuration file in Paths.PATH_CFG.
     * @throws IOException if make new directory fail.
     */
    public static void createEmptyConfigurationFile() throws IOException {
        // create file ~/.jdrive/.jdrive.conf
        Path p = java.nio.file.Paths.get(Paths.PATH_CFG);
        Files.createFile(p);
    }
}
