package it.hackcaffebabe.jdrive.mapping;

import it.hackcaffebabe.jdrive.Constants;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * TODO add doc
 */
public class PathSanitizer implements Sanitizer {
    private String base;
    private final Logger log = LogManager.getLogger();

    PathSanitizer(){
        String watchedDir = (String) Configurator.getInstance()
                                                .get(Keys.WATCHED_BASE_PATH);
        base = watchedDir.replaceFirst(
            Constants.APP_DEFAULT_WATCHED_DIR_NAME, ""
        );
    }

    @Override
    public String sanitize( String toSanitize ) {
        log.debug("Try to sanitize path="+toSanitize);
        if( toSanitize.startsWith(base) ){
            String sanitized = toSanitize.replaceFirst(base, "");
            log.debug("Path sanitized="+sanitized);
            return sanitized;
        }else{
            log.debug("Noting to sanitize: path not start with base");
            return toSanitize;
        }
    }

    String restore( String sanitized ) {
        log.debug("Try to restore path="+sanitized);
        String restored = base+sanitized;
        log.debug("Restored path="+restored);
        return restored;
    }
}
