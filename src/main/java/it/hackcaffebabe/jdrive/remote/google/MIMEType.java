package it.hackcaffebabe.jdrive.remote.google;

import java.util.HashMap;

public class MIMEType
{
    public static final String SPREADSHEET = "application/vnd.google-apps.spreadsheet";
    public static final String DOCUMENT = "application/vnd.google-apps.document";
    public static final String DRAWING = "application/vnd.google-apps.drawing";
    public static final String PRESENTATION = "application/vnd.google-apps.presentation";
    public static final String FOLDER = "application/vnd.google-apps.folder";

    public static HashMap<String, String> Conversion = new HashMap<String, String>() {{
        put( DOCUMENT, "application/pdf" );
        put( SPREADSHEET, "application/pdf" );
        put( DRAWING, "image/png" );
        put( PRESENTATION, "application/pdf" );
        put( "txt", "text/plain"); // TODO add more reverse conversion
    }};
}
