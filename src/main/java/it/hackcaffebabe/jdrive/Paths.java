package it.hackcaffebabe.jdrive;

/**
 * Utility class to provide the basic path of the program
 */
public class Paths
{
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String SEP = System.getProperty("file.separator");

    public static final String DIR_NAME = ".jdrive";
    public static final String PATH_APP = USER_HOME +SEP+ DIR_NAME;

    public static final String PATH_CFG= PATH_APP +SEP+ "jdrive.conf";
}
