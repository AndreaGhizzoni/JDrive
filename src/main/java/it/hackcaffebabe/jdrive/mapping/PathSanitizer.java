package it.hackcaffebabe.jdrive.mapping;

import it.hackcaffebabe.jdrive.Constants;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;

/**
 * This class removes the base string from the beginning of each string to
 * sanitize and restore it.
 */
class PathSanitizer implements Sanitizer
{
    private String base;

    /** Instance a default PathSanitizer */
    PathSanitizer(){
        String watchedDir = (String) Configurator.getInstance()
                                                .get(Keys.WATCHED_BASE_PATH);
        base = watchedDir.replaceFirst(
            Constants.APP_DEFAULT_WATCHED_DIR_NAME, ""
        );
    }

    @Override
    public String sanitize( String toSanitize ) {
        String sanitized = toSanitize;
        if( toSanitize.startsWith(base) ){
            sanitized = toSanitize.replaceFirst( base, "" );
        }
        return sanitized;
    }

    /**
     * Restore the non sanitized path from a sanitized one.
     * @param sanitized {@link java.lang.String} a sanitized path.
     * @return {@link java.lang.String} the non sanitized path.
     */
    String restore( String sanitized ) { return base+sanitized; }
}
