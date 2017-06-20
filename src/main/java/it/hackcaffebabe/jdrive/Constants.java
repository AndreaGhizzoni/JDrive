package it.hackcaffebabe.jdrive;

import static it.hackcaffebabe.jdrive.util.PathsUtil.SEP;
import static it.hackcaffebabe.jdrive.util.PathsUtil.USER_HOME;

/**
 * Application general constants.
 */
public class Constants
{
    /** Path home application folder */
    public static final String APP_HOME = USER_HOME + SEP + ".jdrive";
    /** Path to application configuration file */
    public static final String APP_PROPERTIES_FILE = APP_HOME + SEP + "jdrive.conf";

}
