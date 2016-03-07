package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;

/**
 * NB: this class needs his own basic folder in (user home)/.jdrive
 *
 * How to use:
 * <pre>{@code
 * Paths.createApplicationHomeDirectory(); // this class needs a folder (user home)/.jdrive
 * Configuration c = Configuration.getInstance();
 * c.load();
 * String s = c.get("key");
 * }</pre>
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

    /* Basic constructor */
    private Configurator(){
        //set config file to default
        this.cfgFile = new File(PathsUtil.APP_CGF_FILE);
        log.info("Configuration init");
    }

//==============================================================================
//  METHOD
//==============================================================================
    /**
     * Load data from configuration file in Paths.APP_CGF_FILE. If this file exists,
     * the configuration will be loaded from there, otherwise will be create a
     * new file with the default configuration.
     * @return true if configuration file will be successfully parsed, otherwise
     * if load() method is called multiple times or loading defaults data rise an
     * exception, will return false.
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

        // if file exists the cfgProp already holds the cfg of values
        if(!cfgFile.exists()){
            loadDefault();
        }else{
            loadConfigurationFromFile();
        }

        return true;
    }

    /* this method is used to create a new configuration file if NOT EXISTS
     * whit default configuration*/
    private void loadDefault(){
        log.info("User configuration not Found. Try to load default...");
        try{
            PathsUtil.createEmptyConfigurationFile();
            // load default settings from Default class
            for(Map.Entry<String, Object> i : Default.cfg.entrySet() ){
                put(i.getKey(), i.getValue());
            }
            log.info("Configuration file create and loaded properly.");
        }catch( FileAlreadyExistsException fae ){
            log.error(fae.getMessage());
        }catch( IOException ioe ){
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
                log.error("Value for \""+i.getKey()+"\" is missing: restoring default.");
                put(i.getKey(), i.getValue());
            }
        }
        log.info("Configuration file create and loaded properly.");
    }

    /**
     * Check if given key is associated with existing value in the cfg.
     * @param key {@link java.lang.String} the key as string.
     * @return true if exists, otherwise false.
     * @throws java.lang.IllegalStateException if load method is not called fist.
     */
    public boolean exists(String key) throws IllegalStateException{
        checkLoaded();
        return get(key)!= null;
    }

    /**
     * Remove the value from key given.
     * @param key {@link java.lang.String} the key  of properties
     * @throws java.lang.IllegalStateException if load method is not called fist.
     */
    public void remove(String key) throws  IllegalStateException{
        checkLoaded();
        if(key != null && !key.isEmpty()) {
            this.cfgProp.clearProperty(key);
        }
    }

    /*
     * check if method load() is called.
     */
    private void checkLoaded() throws IllegalStateException{
        if(this.cfgProp == null)
            throw new IllegalStateException("Configuration File not loaded. " +
                    "Call Configuration.getInstance().load() first.");
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
     * @throws java.lang.IllegalStateException if load method is not called fist.
     */
    public boolean put(String key, Object obj) throws IllegalStateException {
        checkLoaded();
        if(key == null || key.isEmpty())
            return false;

        boolean hasOverride = exists(key);
        this.cfgProp.setProperty(key, obj);
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
     * @throws java.lang.IllegalStateException if load method is not called fist.
     */
    public Object get(String key) throws IllegalStateException{
        checkLoaded();
        return this.cfgProp.getProperty(key);
    }
}
