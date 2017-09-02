package it.hackcaffebabe.jdrive.remote.google;

import java.util.HashMap;

/**
 * This class provide a standard way to map remote and local files.
 */
public class MIMEType
{
    public static final String GOOGLE_OPEN_DOC = "application/vnd.oasis.opendocument.spreadsheet";
    public static final String GOOGLE_SPREADSHEET = "application/vnd.google-apps.spreadsheet";
    public static final String GOOGLE_EXCEL = "application/vnd.ms-excel";
    public static final String GOOGLE_EXCEL_2010 = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String GOOGLE_DOCUMENT = "application/vnd.google-apps.document";
    public static final String GOOGLE_WORD = "application/msword";
    public static final String GOOGLE_DRAWING = "application/vnd.google-apps.drawing";
    public static final String GOOGLE_PRESENTATION = "application/vnd.google-apps.presentation";
    public static final String GOOGLE_FOLDER = "application/vnd.google-apps.folder";
    public static final String GOOGLE_OCTET_STREAM = "application/octet-stream";
    public static final String GOOGLE_PLAIN_TEXT = "text/plain";
    public static final String GOOGLE_HTML = "text/html";
    public static final String GOOGLE_XML = "text/xml";
    public static final String GOOGLE_PDF = "application/pdf";
    public static final String GOOGLE_PNG = "image/png";
    public static final String GOOGLE_BMP = "image/bmp";
    public static final String GOOGLE_JPG = "image/jpeg";
    public static final String GOOGLE_GIF = "image/gif";
    public static final String GOOGLE_ZIP = "application/zip";
    public static final String GOOGLE_RAR = "application/rar";
    public static final String GOOGLE_TAR = "application/tar";
    public static final String GOOGLE_ARJ = "application/arj";
    public static final String GOOGLE_CAB = "application/cab";
    public static final String GOOGLE_JAVASCRIPT = "application/js";
    public static final String GOOGLE_PHP = "application/x-http-php";
    public static final String GOOGLE_FLASH = "application/x-shockwave-flash";
    public static final String GOOGLE_MPEG = "audio/mpeg";

    private static HashMap<String, String> conversion = new HashMap<String, String>() {{
        put( GOOGLE_DOCUMENT, GOOGLE_PDF );
        put( GOOGLE_SPREADSHEET, GOOGLE_PDF );
        put( GOOGLE_DRAWING, GOOGLE_PNG );
        put( GOOGLE_PRESENTATION, GOOGLE_PDF );
        put( "xls", GOOGLE_EXCEL );
        put( "xlsx", GOOGLE_EXCEL_2010 );
        put( "xml", GOOGLE_XML );
        put( "ods", GOOGLE_OPEN_DOC );
        put( "txt", GOOGLE_PLAIN_TEXT );
        put( "", GOOGLE_PLAIN_TEXT );
        put( "csv", GOOGLE_PLAIN_TEXT );
        put( "tmpl", GOOGLE_PLAIN_TEXT );
        put( "pdf", GOOGLE_PDF );
        put( "php", GOOGLE_PHP );
        put( "jpg", GOOGLE_JPG );
        put( "png", GOOGLE_PNG );
        put( "gif", GOOGLE_GIF );
        put( "bmp", GOOGLE_BMP );
        put( "doc", GOOGLE_WORD );
        put( "js", GOOGLE_JAVASCRIPT );
        put( "swf", GOOGLE_FLASH );
        put( "mp3", GOOGLE_MPEG );
        put( "zip", GOOGLE_ZIP );
        put( "rar", GOOGLE_RAR );
        put( "tar", GOOGLE_TAR );
        put( "arj", GOOGLE_ARJ );
        put( "cab", GOOGLE_CAB );
        put( "html", GOOGLE_HTML );
        put( "htm", GOOGLE_HTML );
        put( "folder", GOOGLE_FOLDER);
    }};

    /**
     * This method convert a remote or local mime type into a remote or local
     * mime type.
     * @param mimeType {@link java.lang.String} mime type to convert.
     * @return {@link java.lang.String} the converted mime type.
     */
    public static String convert( String mimeType ) {
        mimeType = mimeType.toLowerCase();
        return conversion.getOrDefault( mimeType, GOOGLE_OCTET_STREAM );
    }
}
