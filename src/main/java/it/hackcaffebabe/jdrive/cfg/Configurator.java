package it.hackcaffebabe.jdrive.cfg;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;


/**
 * NB: this class needs his own basic folder in (user home)/.jdrive
 * TODO add description and HOW TO USE
 */
public final class Configurator
{
    private static Logger log = LogManager.getLogger(Configurator.class.getSimpleName());
    private static Configurator instance;

    //set config file to default
    private File cfgFile = new File(Default.PATH_CFG);
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
            //if this rise an Exception, load default configuration
            this.cfg = new PropertiesConfiguration(cfgFile);
            this.loadFromFile();
        }catch (ConfigurationException e){
            log.error(e.getMessage());
            this.loadDefault();
        }
    }

    /* this method is used to parse the EXISTING configuration file */
    private void loadFromFile(){

    }

    /* this method is used to create a new configuration file if NOT EXISTS
     * whit default configuration*/
    private void loadDefault(){

    }
}
