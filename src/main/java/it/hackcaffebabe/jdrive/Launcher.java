package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.applicationutil.Locker;
import it.hackcaffebabe.applicationutil.Util;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.fs.DetectedEvent;
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
        Locker locker = new Locker("JDriveApplication");
        if(locker.isAlreadyRunning()){
            // TODO in the future this will change in something else.
            fatal("JDrive already running", null);
        }

        try{
            log.info("JDrive Application Starting.");
            log.debug("pid: "+Util.getProcessID());
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
            // process that will start from main application
            final Watcher w = Watcher.getInstance();

            // add a shutdown hook to close all the process above properly
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.debug("JDrive closing procedure...");
                    try {
                        w.kill();
                    } catch (IOException e) {
                        fatal("Attempting to kill Watcher process.", e);
                    }
                }
            }, "Main-Shutdown-Hook"));

            LinkedBlockingQueue<DetectedEvent> lbq = new LinkedBlockingQueue<>();
            w.setDispatchingQueue(lbq);
            new Thread(w, "Watcher").start();

            DetectedEvent detObj;
            boolean keepRunning = true;
            while(keepRunning){
                detObj = lbq.take();
                log.debug(detObj.toString());
                if( detObj.containError() ){
                    log.info("Error message from Watcher: "+detObj.getMessage());
                    keepRunning = false;
                }
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
