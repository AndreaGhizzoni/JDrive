package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Default values for configuration files.
 */
public final class Default
{
    public static final Map<String, Object> cfg = new HashMap<String, Object>();

    static{
        cfg.put("base", Paths.USER_HOME+Paths.SEP+"Google Drive");

        // add default settings here
    }
}
