package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.Paths;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * Returns the instance of Configurator.
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
     * This method populate the map with the value from configuration file
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

        this.cfgMap.put("base", cfg.getString("base"));
        //add settings here

        log.info("Configuration file loaded properly.");
    }

    /* this method is used to create a new configuration file if NOT EXISTS
     * whit default configuration*/
    private void loadDefault(){
        log.info("Try to load default configuration...");
        try{
            Path p = java.nio.file.Paths.get(Paths.PATH_CFG);
            //Files.createDirectories(p.getParent());
            Files.createFile(p);

            PropertiesConfiguration cfg = new PropertiesConfiguration(cfgFile);
            this.cfgMap.put("base", Default.BASE);
            cfg.addProperty("base", Default.BASE);

            cfg.save();
            // add default settings here
        }catch( FileAlreadyExistsException fae ){
            log.error(fae.getMessage());
        }catch( IOException ioe ){
            log.error(ioe.getMessage());
        }catch( ConfigurationException ce ){
            log.error(ce.getMessage());
        }
        log.info("Configuration file create and loaded properly.");
    }

    /**
     * Returns the value of the specific key given.
     * @param key {@link java.lang.String} the key.
     * @return {@link java.lang.Object} the object of specific key or null if
     *                                  the key is null or not found.
     */
    public Object get(String key){
        return this.cfgMap.get(key);
    }
}
