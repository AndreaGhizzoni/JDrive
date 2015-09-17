package it.hackcaffebabe.jdrive.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class to manage date and his string representation
 */
public final class DateUtils {
    public static final String DATE_FORMAT = "yyyy.MM.dd 'at' HH:mm:ss";

    public static String fromLongToString(long timestamp, String dateFormat){
        SimpleDateFormat sdf = new SimpleDateFormat(
            dateFormat == null ? DATE_FORMAT : dateFormat
        );
        return sdf.format( new Date(timestamp) );
    }
}
