package it.hackcaffebabe.jdrive;

import java.io.File;
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

    public static final String PATH_APP = USER_HOME +SEP+ ".jdrive";
    public static final String PATH_CFG= PATH_APP +SEP+ "jdrive.conf";

    /**
     * This method check if PATH_APP exists, if not creates it.
     * @throws IOException if mkdirs returns false.
     */
    public static void buildWorkingDirectory() throws IOException{
        Path p = java.nio.file.Paths.get(PATH_APP);
        Files.createDirectories(p);
//        File f = new File( PATH_APP );
//        if(!f.exists() && !f.mkdirs()){
//            throw new IOException("Fail to build working directory "+ PATH_APP);
//        }
    }
}
