package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.fs.Watcher;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * JDrive Application Launcher
 */
public class Launcher {
    private static Logger log = LogManager.getLogger(
        Launcher.class.getSimpleName()
    );

    public static void main( String... args ){
        try{
            PathsUtil.createApplicationHomeDirectory();
        }catch( IOException ioE ){
            fatal(ioE.getMessage(), ioE);
        }

        boolean cfgOK = Configurator.getInstance().load();
        if( !cfgOK )
            fatal("Configurator Error. Program Exit.", null);

        try{
            Thread watcherThread = new Thread(Watcher.getInstance());
            watcherThread.start();
            Thread.sleep(1000); // debug purpose
        }catch( Exception ex ){
            fatal(ex.getMessage(), ex);
        }
    }

//==============================================================================
//  UTILITY METHODS
//==============================================================================
    // this method write a fatal message into log file and kill the program
    private static void fatal(String msg, Throwable t){
        log.fatal(msg, t);
        System.exit(-1);
    }
}
