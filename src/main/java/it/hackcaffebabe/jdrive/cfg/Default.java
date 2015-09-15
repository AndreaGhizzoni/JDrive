package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * Default and required values for configuration files.
 */
public final class Default
{
    public static final Map<String, Object> cfg = new HashMap<String, Object>();

    static{
        cfg.put("base", PathsUtil.USER_HOME+ PathsUtil.SEP+"Google Drive");

        // add default settings here
    }
}
