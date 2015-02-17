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
    private PropertiesConfiguration cfgProp;
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
        if(this.cfgProp != null )
            return; // safe condition: avoid multiple calls of load()

        try{
            this.cfgProp = new PropertiesConfiguration(cfgFile);
            this.cfgProp.setAutoSave(true);
        }catch (ConfigurationException e) {
           log.error(e.getMessage());
        }

        if(cfgFile.exists())
            loadFromFile();
        else
            loadDefault();
    }

    /* this method is used to parse the EXISTING configuration file */
    private void loadFromFile(){
        log.info("Try to load configuration from jdrive.conf...");

        this.cfgMap.put("base", this.cfgProp.getString("base"));
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
        return this.cfgMap.get(key);
    }

    /**
     * Put a new value into the configuration file. If this key already exists,
     * the value associated with will be replaced with the newest one.
     * @param key {@link java.lang.String} the key string
     * @param obj {@link java.lang.Object} to put
     * @return true if there wasn't that object with that key, false otherwise
     */
    public boolean put(String key, Object obj){
        if(key == null || key.isEmpty() )
            return false;

        this.cfgProp.setProperty(key, obj);
        return this.cfgMap.put(key, obj) == null;
    }

    /**
     * Remove the value from key given.
     * @param key {@link java.lang.String} the key  of properties
     */
    public void remove(String key){
        if(key != null && !key.isEmpty()) {
            this.cfgProp.clearProperty(key);
            this.cfgMap.remove(key);
        }
    }
}
