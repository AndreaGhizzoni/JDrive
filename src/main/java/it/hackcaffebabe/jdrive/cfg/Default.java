package it.hackcaffebabe.jdrive.cfg;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Default and required values for configuration files.
 */
public class Default
{
    // utility paths
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String SEP = System.getProperty("file.separator");

    // default application paths
    public static final String APP_HOME = USER_HOME + SEP + ".jdrive";
    public static final String APP_CGF_FILE = APP_HOME + SEP + "jdrive.conf";

    public static final File G_TOKEN = new File( APP_HOME + SEP + "t.json" );

    // map of default configuration
    public static final Map<String, Object> cfg = new HashMap<String, Object>();
    static{
        cfg.put(Keys.WATCHED_DIR, USER_HOME + SEP + "Google Drive");
        // add default settings here
    }
}
