package it.hackcaffebabe.jdrive.auth.google;

import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;
import java.util.List;

import static it.hackcaffebabe.jdrive.util.PathsUtil.APP_HOME;
import static it.hackcaffebabe.jdrive.util.PathsUtil.SEP;

final class AuthenticationConst
{
    /** The application name used in console.developers.google.com */
    static final String APP_NAME = "JDrive";

    /** String of path to json file for google credential */
    static final String DATA_STORE_JSON = APP_HOME + SEP + "t.json";
    /** String of path to local credentials */
    static final String LOCAL_CRED = SEP + "credentials.json";

    /** List of scopes API */
    static final List<String> SCOPES = Arrays.asList(
        DriveScopes.DRIVE,
        DriveScopes.DRIVE_FILE
    );
}
