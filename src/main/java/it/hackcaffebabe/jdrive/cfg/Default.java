package it.hackcaffebabe.jdrive.cfg;

import java.util.HashMap;
import java.util.Map;

/**
 * Default and required values for configuration files.
 */
public class Default
{
    // basic configuration of application home and configuration file
    public static final String USER_HOME = System.getProperty("user.home");
    public static final String SEP = System.getProperty("file.separator");
    public static final String APP_HOME = USER_HOME + SEP + ".jdrive";
    public static final String APP_CGF_FILE = APP_HOME + SEP + "jdrive.conf";

    // map of default configuration
    public static final Map<String, Object> cfg = new HashMap<String, Object>();
    static{
        cfg.put("base", USER_HOME + SEP + "Google Drive");

        // add default settings here
    }
}
