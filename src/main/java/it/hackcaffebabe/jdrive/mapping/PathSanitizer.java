package it.hackcaffebabe.jdrive.mapping;

import it.hackcaffebabe.jdrive.Constants;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;

/**
 * TODO add doc
 */
class PathSanitizer implements Sanitizer {
    private String base;

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
            sanitized = toSanitize.replaceFirst(base, "");
        }
        return sanitized;
    }

    String restore( String sanitized ) {
        String restored = base+sanitized;
        return restored;
    }
}
