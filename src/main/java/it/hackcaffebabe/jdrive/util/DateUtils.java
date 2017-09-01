package it.hackcaffebabe.jdrive.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class to manage date and his string representation
 */
public final class DateUtils
{
    public static final String DEFAULT_FORMAT = "yyyy.MM.dd 'at' HH:mm:ss.SSSSSS";

    /**
     * This method convert from long timestamp in a formatted date string.
     * Default format is: yyyy.MM.dd 'at' HH:mm:ss
     * @param timestamp {@link java.lang.Long} the timestamp
     * @return {@link java.lang.String} of formatted timestamp
     */
    public static String formatTimestamp( long timestamp ){
        return formatTimestamp( timestamp <= 0L ? 0 : timestamp, DEFAULT_FORMAT );
    }

    /**
     * This method convert from long timestamp in a formatted date string
     * according to format parameter.
     * @param timestamp {@link java.lang.Long} the timestamp
     * @param format {@link java.lang.String} how to format the timestamp
     * @return {@link java.lang.String} of formatted timestamp
     */
    public static String formatTimestamp( long timestamp, String format ){
        return new SimpleDateFormat(format).format(
            new Date(timestamp <= 0L ? 0 : timestamp)
        );
    }
}
