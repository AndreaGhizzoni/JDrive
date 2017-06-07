package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Configurator class manage Properties file for your application.
 * Simple usage:
 * <pre>{@code
 * File p = new File("cfg.prop");
 * try{
 *     Configurator c = Configurator.setup(p);
 * }catch( ConfigurationException e ){
 *     e.printStackTrace();
 * }
 * c.put("key", "value")
 * System.out.println(c.get("key"));
 * }</pre>
 */
public final class Configurator
{
    private static Logger log = LogManager.getLogger();
    private static Configurator instance;
    private static PropertiesConfiguration propConfiguration;
    private static Path pathConfigurationFile;

    /**
     * Returns the instance of Configurator.
     * @throws IllegalStateException if setup() method is not called first.
     */
    public static Configurator getInstance() throws IllegalStateException{
        log.info("Configurator instance requested.");
        if( propConfiguration == null )
            throw new IllegalStateException("Configurator need to be set up "+
                    "via Configurator.setup() method first.");
        if( instance == null )
            instance = new Configurator();
        return instance;
    }

    /**
     * This method create a completely functional Configurator object just
     * giving the properties file path as argument. Multiple called of this
     * method will reset the previous one.
     * @param configurationFilePath {@link java.nio.file.Path} the configuration
              file path.
     * @return {@link it.hackcaffebabe.jdrive.cfg.Configurator} the instance of
     *         Configurator.
     * @throws IllegalArgumentException if argument is null or a directory
     * @throws ConfigurationException if something else went wrong
     */
    public static Configurator setup( Path configurationFilePath )
            throws IllegalArgumentException, ConfigurationException {

        if( configurationFilePath == null )
            throw new IllegalArgumentException("Configuration file passed as " +
                    "argument can not be null");

        if( PathsUtil.isDirectory(configurationFilePath) )
            throw new IllegalArgumentException("Configuration file passed as " +
                    "argument is a directory.");

        pathConfigurationFile = configurationFilePath.toAbsolutePath();
        log.info("Configurator setup called with file: "
                +pathConfigurationFile.toString());

        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
            new FileBasedConfigurationBuilder<>(
                    PropertiesConfiguration.class
            ).configure(
                new Parameters().properties().setFileName(
                    pathConfigurationFile.toString()
                )
            );
        propConfiguration = builder.getConfiguration();
        log.info("Configurator get from builder.");

        Configurator c = Configurator.getInstance();
        if( pathConfigurationFile.toFile().exists() ){
            c.checkDefault();
        }else{
            c.createNewAndLoadDefault();
        }
        return c;
    }

    private Configurator() {}

//==============================================================================
//  METHOD
//==============================================================================
    /* this method is used to create a new configuration file if NOT EXISTS
     * whit default configuration */
    private void createNewAndLoadDefault(){
        log.info("User configuration not found. Try to load default...");
        try{
            Files.createFile(pathConfigurationFile);
            for( Map.Entry<String, Object> i : Default.cfg.entrySet() ){
                put( i.getKey(), i.getValue() );
            }
            log.info("Configuration file create and loaded properly.");
        } catch( IOException ioe ){
            log.error( ioe.getMessage() );
        }
    }

    /* this method check if the required value are set in the configuration file.
     * If not, restores the defaults. */
    private void checkDefault(){
        log.info("User configuration found. Try to load...");
        for( Map.Entry<String, Object> i : Default.cfg.entrySet() ){
            if( !exists(i.getKey()) ) {
                log.info("Value for \""+i.getKey()+"\" is missing: restoring "+
                        "default.");
                put( i.getKey(), i.getValue() );
            }
        }
        log.info("Configuration file create and loaded properly.");
    }

    /**
     * Check if given key is associated with existing value in the cfg.
     * @param key {@link java.lang.String} the key as string.
     * @return true if exists, otherwise false.
     */
    public boolean exists( String key ) {
        return get( key )!= null;
    }

    /**
     * Remove the value from key given.
     * @param key {@link java.lang.String} the key  of properties
     */
    public void remove( String key ) {
        if( key != null && !key.isEmpty() ) {
            propConfiguration.clearProperty( key );
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
     * @return true if there was a object with that key, false otherwise
     */
    public boolean put( String key, Object obj ) {
        if( key == null || key.isEmpty() )
            return false;

        boolean hasOverride = exists( key );
        propConfiguration.setProperty( key, obj );
        log.debug("Loaded > Key: "+ key +" : \""+ obj.toString() +"\"");
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
    public Object get( String key ){
        return propConfiguration.getProperty( key );
    }
}
