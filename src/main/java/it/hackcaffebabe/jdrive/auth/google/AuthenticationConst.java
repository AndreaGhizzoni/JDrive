package it.hackcaffebabe.jdrive.auth.google;

import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;
import java.util.List;

final class AuthenticationConst
{
    /** The application name used in console.developers.google.com */
    static final String APP_NAME = "JDrive";

    /** List of scopes API */
    static final List<String> SCOPES = Arrays.asList(
        DriveScopes.DRIVE,
        DriveScopes.DRIVE_FILE
    );
}
