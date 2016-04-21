package it.hackcaffebabe.jdrive.auth.google;

import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;
import java.util.List;

final class AuthenticationConst
{
    /** Directory to store user credentials for this application. */
    static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"),
            ".jdrive/cred"
    );

    /** the application name */
    static final String APP_NAME = "JDrive";

    /** list of Scopes */
    static final List<String> SCOPES = Arrays.asList(
            DriveScopes.DRIVE,
            DriveScopes.DRIVE_FILE
    );
}
