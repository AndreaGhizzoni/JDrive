package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.util.PathsUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Default and required values for configuration files.
 */
public class Default
{
    // map of default configuration
    public static final Map<String, Object> PROPERTIES = new HashMap<String, Object>(){
        {
            put(
                Keys.WATCHED_BASE_PATH,
                PathsUtil.USER_HOME + PathsUtil.SEP + "Google Drive"
            );
        }
    };
}
