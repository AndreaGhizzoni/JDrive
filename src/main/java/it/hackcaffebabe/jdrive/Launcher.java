package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.fs.DetectedObject;
import it.hackcaffebabe.jdrive.fs.watcher.Watcher;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JDrive Application Launcher
 */
public class Launcher {
    private static Logger log = LogManager.getLogger(
        Launcher.class.getSimpleName()
    );

    public static void main( String... args ){
        try{
            log.info("JDrive Application Starting.");
            PathsUtil.createApplicationHomeDirectory();
            log.info("JDrive Home directory created/detected in: "+PathsUtil.APP_HOME);
        }catch( IOException ioE ){
            fatal(ioE.getMessage(), ioE);
        }

        boolean cfgOK = Configurator.getInstance().load();
        if( !cfgOK )
            fatal("Configurator Error. Program Exit.", null);

        // TODO insert here Google Authentication process

        try{
            LinkedBlockingQueue<DetectedObject> lbq = new LinkedBlockingQueue<DetectedObject>();
            Watcher w = Watcher.getInstance();
            w.setDispatchingQueue(lbq);

            Thread watcherThread = new Thread(w);
            watcherThread.start();

            DetectedObject detObj;
            while(true){
                detObj = lbq.take();
                log.debug(detObj.toString());
            }
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
