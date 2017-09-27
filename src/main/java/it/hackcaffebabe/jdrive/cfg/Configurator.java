package it.hackcaffebabe.jdrive.cfg;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Configurator class manage Properties file for your application.
 * Simple usage:
 * <pre>{@code
 * Path path = Paths.get( "pro.properties" );
 * try{
 *     Configurator c = Configurator.setup( path );
 * }catch( IllegalArgumentException | IOException e ){
 *     e.printStackTrace();
 * }
 * c.put( "key", "value" )
 * System.out.println( c.get("key") );
 * }</pre>
 */
public final class Configurator
{
    private static Logger log = LogManager.getLogger();
    private static Configurator instance;
    private static Configuration configuration;

    /**
     * Returns the current instance of Configurator.
     * @throws IllegalStateException if setup() method is not called first.
     */
    public static Configurator getInstance() throws IllegalStateException{
        if( configuration == null )
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
     * @throws IOException if something went wrong with parsing properties file
     */
    public static Configurator setup( Path configurationFilePath )
            throws IllegalArgumentException, IOException{

        if( configurationFilePath == null )
            throw new IllegalArgumentException("Configuration file passed as " +
                    "argument can not be null");

        if( PathsUtil.isDirectory(configurationFilePath) )
            throw new IllegalArgumentException("Configuration file passed as " +
                    "argument is a directory.");

        Path pathPropertiesFile = configurationFilePath
                .normalize().toAbsolutePath();
        log.info("Configurator setup called with file: "
                + pathPropertiesFile.toString());

        if( !pathPropertiesFile.toFile().exists() ){
            log.info("Properties file doesn't exists, try to create one.");
            Files.createFile(pathPropertiesFile);
            log.info("Empty properties file created.");
        }

        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
            new FileBasedConfigurationBuilder<FileBasedConfiguration>
                    (PropertiesConfiguration.class).configure(
                params.properties().setFile(pathPropertiesFile.toFile())
            );
        builder.setAutoSave(true);
        try {
            configuration = builder.getConfiguration();
        }catch (ConfigurationException cause){
            throw new IOException( cause );
        }
        log.info("Configurator get from builder.");

        instance = new Configurator();
        instance.checkRequiredProperties();
        return instance;
    }

    private Configurator() {
        log.info("Configurator creation.");
    }

    /**
     * Put a new value into the properties file. If this key already exists,
     * the value associated with will be replaced with the newest one.
     * @param key {@link java.lang.String} the key string
     * @param obj {@link java.lang.Object} to object to put
     * @return true if there was a object with that key, false otherwise or key
     *         is null or empty string.
     */
    public boolean put( String key, Object obj ) {
        if( key == null || key.isEmpty() )
            return false;

        boolean hasOverride = exists( key );
        configuration.setProperty( key, obj );
        log.debug("Loaded > Key: "+ key +" : \""+ obj.toString() +"\"");
        return hasOverride;
    }

    /**
     * Returns the value of the specific key given, or null if there isn't no object
     * associated with given key.
     * @param key {@link java.lang.String} the key.
     * @return {@link java.lang.Object} the object of specific key or null if
     *                                  the key is null or not found.
     */
    public Object get( String key ){ return configuration.getProperty( key ); }

    /**
     * Remove the value from key given if is not null and not empty string.
     * @param key {@link java.lang.String} the key of properties
     */
    public void remove( String key ) {
        if( key != null && !key.isEmpty() ) {
            configuration.clearProperty( key );
        }
    }

    /**
     * Check if given key is associated with some value in the properties file.
     * @param key {@link java.lang.String} the key to check
     * @return true if exists, false otherwise
     */
    public boolean exists( String key ) { return get( key )!= null; }

    /**
     * Returns the map representation of actual properties stored.
     * @return {@link java.util.Map} the map of key-value properties.
     */
    public Map<String, Object> entrySet(){
        Iterator<String> keys = configuration.getKeys();
        Map<String, Object> maps = new HashMap<>();
        while( keys.hasNext() ){
            String key = keys.next();
            maps.put( key, get(key) );
        }
        return maps;
    }

    /**
     * Return the number of keys stored in the properties file.
     * @return {@link java.lang.Integer} the number of keys in properties file.
     */
    public Integer size(){ return configuration.size(); }

    /* check if default and required properties are set in the properties file*/
    private void checkRequiredProperties() {
        log.info("Checking if there are default properties in the file...");
        for( Map.Entry<String, Object> i : Default.PROPERTIES.entrySet() ){
            if( !exists(i.getKey()) ) {
                log.info("Value for \""+i.getKey()+"\" is missing: restoring "+
                        "default.");
                put( i.getKey(), i.getValue() );
            }
        }
        log.info("Properties file create and loaded properly.");
    }
}
