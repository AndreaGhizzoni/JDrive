package it.hackcaffebabe.jdrive.auth.google;

import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;
import java.util.List;

import static it.hackcaffebabe.jdrive.Constants.APP_HOME;
import static it.hackcaffebabe.jdrive.util.PathsUtil.SEP;

final class AuthenticationConst
{
    /** The application name used in console.developers.google.com */
    static final String APP_NAME = "JDrive";

    /** Directory path as string where google credential will be stored. */
    static final String DATA_STORE_JSON = APP_HOME + SEP + "cred";
    /** Path as string of local credentials */
    static final String LOCAL_CRED = SEP + "credentials.json";

    /** List of scopes API */
    static final List<String> SCOPES = Arrays.asList(
        DriveScopes.DRIVE,
        DriveScopes.DRIVE_FILE,
        DriveScopes.DRIVE_APPDATA,
        DriveScopes.DRIVE_SCRIPTS,
        DriveScopes.DRIVE_METADATA
    );
}
