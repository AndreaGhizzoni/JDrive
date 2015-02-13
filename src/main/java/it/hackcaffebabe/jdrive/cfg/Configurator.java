package it.hackcaffebabe.jdrive.cfg;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;


/**
 * commons.apache.org/proper/commons-configuration/userguide_v1.10/howto_properties.html#Properties_files
 */
public final class Configurator
{
    private static Logger log = LogManager.getLogger(Configurator.class.getSimpleName());
    private static Configurator instance;

    private PropertiesConfiguration cfg;

    /**
     * TODO add doc
     */
    public static Configurator getInstance(){
        if(instance==null)
            instance = new Configurator();
        return instance;
    }

    /* TODO add description */
    private Configurator(){
        try{
            // if file.properties exists
            // than load it
            // else load default configurations
            this.cfg = new PropertiesConfiguration("");
        }catch (ConfigurationException e){
            log.error(e.getMessage());
        }
    }
}
