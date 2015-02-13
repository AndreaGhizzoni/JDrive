package it.hackcaffebabe.jdrive.cfg;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;


/**
 * NB: this class needs his own basic folder in (user home)/.jdrive
 *
 * commons.apache.org/proper/commons-configuration/userguide_v1.10/howto_properties.html#Properties_files
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
            this.cfg = new PropertiesConfiguration(cfgFile);

            if(cfgFile.exists()){
                this.loadFromFile();
            }else{
                this.loadDefault();
            }
        }catch (ConfigurationException e){
            log.error("Error while parsing configuration file!");
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
