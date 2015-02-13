package it.hackcaffebabe.jdrive;

import java.io.File;
import java.io.IOException;

/**
 * Utility class to provide the basic path of the program
 */
public class Paths
{
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String SEP = System.getProperty("file.separator");

    public static final String PATH_APP = USER_HOME +SEP+ ".jdrive";
    public static final String PATH_CFG= PATH_APP +SEP+ "jdrive.conf";

    public static void build() throws IOException{
        File f = new File( PATH_APP );
        if(!f.exists() && !f.mkdirs()){
            throw new IOException("Fail to build working directory "+ PATH_APP);
        }
    }
}
