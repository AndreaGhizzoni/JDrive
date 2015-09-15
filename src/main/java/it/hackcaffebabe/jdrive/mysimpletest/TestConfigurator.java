package it.hackcaffebabe.jdrive.mysimpletest;

import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Testing Configurator.
 */
public class TestConfigurator
{
    public static final Logger log = LogManager.getLogger("MyTest");

    public static void main( String...args){
        try{
            PathsUtil.buildWorkingDirectory();
            Configurator c = Configurator.getInstance();
            c.load();

            log.debug("Base retried from cfg file is "+ c.get(Keys.WORKING_DIR));
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
