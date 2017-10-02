package it.hackcaffebabe.jdrive.remote.google;

import java.util.HashMap;

/**
 * This class provide a standard way to map remote and local files.
 */
public class MIMEType
{
    public static enum Local {
        FOLDER( "folder" ),

        DOC( "doc" ), DOCX( "docx" ),
        XLS( "xls" ), XLSX( "xlsx" ),
        PPT( "ppt" ), PPTX( "pptx" )

        ;
        private String mimeType;
        Local( final String localMimeType ){ this.mimeType = localMimeType; }
        @Override public String toString() { return this.mimeType; }
    }

    public static enum Remote {
        FOLDER( "application/vnd.google-apps.folder" ),

        DOCUMENT( "application/vnd.google-apps.document" ),
        SPREADSHEET( "application/vnd.google-apps.spreadsheet" ),
        PRESENTATION( "application/vnd.google-apps.presentation" ),
        DRAWING( "application/vnd.google-apps.drawing" ),

        WORD( "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ),
        POWER_POINT( "application/vnd.openxmlformats-officedocument.presentationml.presentation" ),
        EXCEL( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ),

        PNG( "image/png" )

        ;
        private final String mimeType;
        Remote( final String localMimeType ){ this.mimeType = localMimeType; }
        @Override public String toString() { return this.mimeType; }
    }

    private static HashMap<String, String> conversion = new HashMap<>();

    static {{
        //======================================================================
        // Remote -> Local (download)
        put( Remote.FOLDER, Local.FOLDER );

        // google documents
        put( Remote.DOCUMENT, Remote.WORD );
        put( Remote.SPREADSHEET, Remote.EXCEL );
        put( Remote.PRESENTATION, Remote.POWER_POINT );
        put( Remote.DRAWING, Remote.PNG );

        //======================================================================
        // Remote <- Local (upload)
        put( Local.FOLDER, Remote.FOLDER );

        // documents
        put( Local.DOC, Remote.DOCUMENT );
        put( Local.DOCX, Remote.DOCUMENT );
        put( Local.XLS, Remote.SPREADSHEET );
        put( Local.XLSX, Remote.SPREADSHEET );
        put( Local.PPT, Remote.PRESENTATION );
        put( Local.PPTX, Remote.PRESENTATION );
    }}

    private static void put( Enum a, Enum b ){
        conversion.put( a.toString(), b.toString() );
    }

    /**
     * This method convert a remote mime type into a local mime type and vice
     * versa. If this method returns empty string means that there is not such
     * a conversion for given mime type.
     * @param mimeType {@link java.lang.String} mime type to convert.
     * @return {@link java.lang.String} the converted mime type.
     */
    public static String convert( String mimeType ) {
        mimeType = mimeType.toLowerCase();
        return conversion.getOrDefault( mimeType, "" );
    }
}
