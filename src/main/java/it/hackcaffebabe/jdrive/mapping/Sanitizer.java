package it.hackcaffebabe.jdrive.mapping;

/**
 * Interface that describe a string sanitizer.
 */
public interface Sanitizer
{
    /**
     * This method take a string toSanitize and return the string sanitized.
     * @param toSanitize {@link java.lang.String} a string to sanitize.
     * @return {@link java.lang.String} the sanitized string.
     */
    String sanitize(String toSanitize);
}
