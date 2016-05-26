package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * TODO add doc and example
 * <pre>{@code
 * }</pre>
 */
public final class Configurator
{
    private static Logger log = LogManager.getLogger();
    private static Configurator instance;
    private static PropertiesConfiguration CFG;

    /**
     * TODO add doc
     * @param cfgFilePath
     * @return
     * @throws IllegalArgumentException
     * @throws ConfigurationException
     */
    public static Configurator setup( File cfgFilePath ) throws
            IllegalArgumentException, ConfigurationException {
        if( cfgFilePath == null )
            throw new IllegalArgumentException("Properties file passed as " +
                    "argument can not be null");

        if( PathsUtil.isDirectory(cfgFilePath) )
            throw new IllegalArgumentException("Properties file passed as " +
                    "argument is a directory.");

        PropertiesConfiguration p = new PropertiesConfiguration(cfgFilePath);
        return Configurator.setup(p);
    }

    /**
     * TODO add doc
     * @param propCfg
     * @return
     * @throws IllegalArgumentException
     */
    public static Configurator setup(PropertiesConfiguration propCfg)
            throws IllegalArgumentException{
        log.info("Configurator setup called on regular file...");

        if( propCfg == null )
            throw new IllegalArgumentException("Properties configuration "+
                    "passed as argument can not be null");

        CFG = propCfg;
        log.info("Configurator properties set.");
        Configurator c = Configurator.getInstance();

        // if file already exists, there are properties into configuration file.
        // otherwise load default properties
        if( !propCfg.getFile().exists() ){
            c.loadDefault();
        }else{
            c.loadConfigurationFromFile();
        }
        return c;
    }

    /**
     * Returns the instance of Configurator.
     */
    public static Configurator getInstance() throws IllegalStateException{
        log.info("Configurator instance requested.");
        if(instance==null)
            instance = new Configurator();
        return instance;
    }

    /* Basic constructor */
    private Configurator() throws IllegalStateException{
        if( CFG == null )
            throw new IllegalStateException("Configurator need to be set first"+
                    " via: Configurator.setup() method.");
        log.info("Configurator instance created.");
    }

//==============================================================================
//  METHOD
//==============================================================================
    /* this method is used to create a new configuration file if NOT EXISTS
     * whit default configuration*/
    private void loadDefault(){
        log.info("User configuration not Found. Try to load default...");
        try{
            Files.createFile(CFG.getFile().toPath());
            // load default settings from Default class
            for(Map.Entry<String, Object> i : Default.cfg.entrySet() ){
                put(i.getKey(), i.getValue());
            }
            log.info("Configuration file create and loaded properly.");
        } catch( IOException ioe ){
            log.error(ioe.getMessage());
        }
    }

    /*
     * This method check if the required value are set in the configuration file.
     * If not, restores the defaults.
     */
    private void loadConfigurationFromFile(){
        log.info("User configuration Found. Try to load...");
        for(Map.Entry<String, Object> i : Default.cfg.entrySet()){
            if(get(i.getKey()) == null) { //miss minimum value
                log.error("Value for \""+i.getKey()+"\" is missing: restoring "+
                        "default.");
                put(i.getKey(), i.getValue());
            }
        }
        log.info("Configuration file create and loaded properly.");
    }

    /**
     * Check if given key is associated with existing value in the cfg.
     * @param key {@link java.lang.String} the key as string.
     * @return true if exists, otherwise false.
     */
    public boolean exists(String key) {
        return get(key)!= null;
    }

    /**
     * Remove the value from key given.
     * @param key {@link java.lang.String} the key  of properties
     */
    public void remove(String key) {
        if(key != null && !key.isEmpty()) {
            CFG.clearProperty(key);
        }
    }

//==============================================================================
//  SETTER
//==============================================================================
    /**
     * Put a new value into the configuration file. If this key already exists,
     * the value associated with will be replaced with the newest one.
     * @param key {@link java.lang.String} the key string
     * @param obj {@link java.lang.Object} to put
     * @return true if there wasn't that object with that key, false otherwise
     */
    public boolean put(String key, Object obj) {
        if(key == null || key.isEmpty())
            return false;

        boolean hasOverride = exists(key);
        CFG.setProperty(key, obj);
        log.debug("Loaded > Key: " + key + " : \"" + obj.toString() + "\"");
        return hasOverride;
    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * Returns the value of the specific key given.
     * @param key {@link java.lang.String} the key.
     * @return {@link java.lang.Object} the object of specific key or null if
     *                                  the key is null or not found.
     */
    public Object get(String key){
        return CFG.getProperty(key);
    }
}
