package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.jdrive.util.PathsUtil;

/**
 * Application general constants.
 */
public class Constants
{
    /** JDrive application version */
    static final String VERSION = "0.1.1-dev";

    /** Path home application folder */
    public static final String APP_HOME = PathsUtil.USER_HOME +
                                          PathsUtil.SEP + ".jdrive";

    /** Path to application configuration file */
    public static final String APP_PROPERTIES_FILE = APP_HOME +
                                                     PathsUtil.SEP + "jdrive.conf";

    public static final String APP_DEFAULT_WATCHED_DIR_NAME = "Google Drive";

    /** Current application pid. Set by Launcher */
    static volatile Long CURRENT_PID = -1L;
}
