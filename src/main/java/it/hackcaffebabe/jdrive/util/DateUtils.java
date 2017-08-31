package it.hackcaffebabe.jdrive.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class to manage date and his string representation
 */
public final class DateUtils {
    /**
     * This method convert from long timestamp in a formatted date string.
     * Default format is: yyyy.MM.dd 'at' HH:mm:ss
     * @param timestamp {@link java.lang.Long} the timestamp
     * @return {@link java.lang.String} of formatted timestamp
     */
    public static String formatTimestamp( long timestamp ){
        if( timestamp <= 0L ) return "0000.00.00 'at' 00:00:00";

        String default_format = "yyyy.MM.dd 'at' HH:mm:ss.SSSSSS";
        return new SimpleDateFormat(default_format).format( new Date(timestamp) );
    }
}
