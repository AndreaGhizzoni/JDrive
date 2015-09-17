package it.hackcaffebabe.jdrive.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class to manage date and his string representation
 */
public final class DateUtils {
    public static final String DATE_FORMAT = "yyyy.MM.dd 'at' HH:mm:ss";

    /**
     * This method convert a timestamp in long format in a formatted date format.
     * Pass null to date format to use the default DateUtils.DATE_FORMAT.
     * @param timestamp {@link java.lang.Long} the timestamp
     * @param dateFormat {@link java.lang.String} string to use to format the
     *                                           timestamp.
     * @return {@link java.lang.String} of formatted timestamp or null if
     * timestamp is <= 0L.
     */
    public static String fromLongToString(long timestamp, String dateFormat){
        if( timestamp <= 0L ) return null;

        SimpleDateFormat sdf = new SimpleDateFormat(
            dateFormat == null || dateFormat.isEmpty() ? DATE_FORMAT : dateFormat
        );
        return sdf.format( new Date(timestamp) );
    }
}
