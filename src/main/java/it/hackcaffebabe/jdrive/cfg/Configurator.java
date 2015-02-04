package it.hackcaffebabe.jdrive.cfg;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * commons.apache.org/proper/commons-configuration/userguide_v1.10/howto_properties.html#Properties_files
 */
public class Configurator
{
    private static Logger log = LogManager.getLogger(Configurator.class.getSimpleName());

    public static void main( String...args){
        try {
            Configuration config  = new PropertiesConfiguration("test.properties");
            log.info(config.getString("path"));
        } catch (ConfigurationException e) {
            log.error(e.getMessage());
        }
    }
}
