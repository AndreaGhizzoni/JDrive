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


/**
 * NB: this class needs his own basic folder in (user home)/.jdrive
 * TODO add description and HOW TO USE
 */
public final class Configurator
{
    private static Logger log = LogManager.getLogger(
        Configurator.class.getSimpleName()
    );
    private static Configurator instance;

    private File cfgFile;
    private PropertiesConfiguration cfgProp;

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
    }

    /**
     * TODO add doc
     * @return
     */
    public boolean load(){
        if(this.cfgProp != null )
            return false; // safe condition: avoid multiple calls of load()

        try{
            this.cfgProp = new PropertiesConfiguration(cfgFile);
            this.cfgProp.setAutoSave(true);
        }catch (ConfigurationException e) {
            log.error(e.getMessage());
            return false;
        }

        // if file exists the cfgProp already holds the map of values
        if(!cfgFile.exists())
            loadDefault();

        return true;
    }

    /* this method is used to create a new configuration file if NOT EXISTS
     * whit default configuration*/
    private void loadDefault(){
        log.info("Try to load default configuration...");
        try{
            Path p = java.nio.file.Paths.get(Paths.PATH_CFG);
            Files.createFile(p);

            put("base", Default.BASE);
            // add default settings here

        }catch( FileAlreadyExistsException fae ){
            log.error(fae.getMessage());
        }catch( IOException ioe ){
            log.error(ioe.getMessage());
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
        return this.cfgProp.getProperty(key);
    }

    /**
     * Put a new value into the configuration file. If this key already exists,
     * the value associated with will be replaced with the newest one.
     * @param key {@link java.lang.String} the key string
     * @param obj {@link java.lang.Object} to put
     * @return true if there wasn't that object with that key, false otherwise
     */
    public boolean put(String key, Object obj){
        boolean hasOverride = exists(key);
        this.cfgProp.setProperty(key, obj);
        return hasOverride;
    }

    /**
     * Check if given key is associated with existing value in the map.
     * @param key {@link java.lang.String} the key as string.
     * @return true if exists, otherwise false.
     */
    public boolean exists(String key){
        return get(key)!= null;
    }

    /**
     * Remove the value from key given.
     * @param key {@link java.lang.String} the key  of properties
     */
    public void remove(String key){
        if(key != null && !key.isEmpty()) {
            this.cfgProp.clearProperty(key);
        }
    }
}
