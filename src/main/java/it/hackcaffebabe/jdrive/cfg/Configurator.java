package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.Paths;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;


/**
 * NB: this class needs his own basic folder in (user home)/.jdrive
 * TODO add description and HOW TO USE
 */
public final class Configurator
{
    private static Logger log = LogManager.getLogger("Configurator");
    private static Configurator instance;

    private File cfgFile;
    private HashMap<String, Object> cfgMap;

    /**
     * TODO add doc
     */
    public static Configurator getInstance(){
        if(instance==null)
            instance = new Configurator();
        return instance;
    }

    /**
     * Basic empty constructor
     */
    private Configurator(){
        //set config file to default
        this.cfgFile = new File(Paths.PATH_CFG);
        this.cfgMap  = new HashMap<String, Object>();
    }

    /**
     * TODO add doc
     */
    public void load(){
        try{
            //if this rise an Exception, load default configuration
            this.loadFromFile( new PropertiesConfiguration(cfgFile) );
        }catch (ConfigurationException e){
            log.error(e.getMessage());
            this.loadDefault();
        }
    }

    /* this method is used to parse the EXISTING configuration file */
    private void loadFromFile(PropertiesConfiguration cfg){
        log.info("Try to load configuration from jdrive.conf...");

    }

    /* this method is used to create a new configuration file if NOT EXISTS
     * whit default configuration*/
    private void loadDefault(){
        log.info("Try to load default configuration...");

    }

    /**
     * TODO add doc
     * @param key
     * @return
     */
    public Object get(String key){
        return this.cfgMap.get(key);
    }
}
