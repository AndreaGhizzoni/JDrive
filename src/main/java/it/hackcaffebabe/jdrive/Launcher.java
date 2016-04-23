package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.applicationutil.Locker;
import it.hackcaffebabe.applicationutil.Util;
import it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.fs.DetectedEvent;
import it.hackcaffebabe.jdrive.fs.watcher.Watcher;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JDrive Application Launcher
 */
public class Launcher {
    private static Logger log = LogManager.getLogger(
        Launcher.class.getSimpleName()
    );

    /* parsing argument from CLI: using -X where X is the argument */
    private static CommandLine argParser(String... args) throws ParseException {
        Options o = new Options();
        o.addOption("status", false, "check JDrive status");
        o.addOption("start", false, "start JDrive");
        o.addOption("stop", false, "stop JDrive");
        return new DefaultParser().parse(o, args);
    }

    public static void main( String... args ){
        try{
            CommandLine cli = argParser(args);
            if( cli.hasOption("start") )
                log.debug("start flag detected");
            else if( cli.hasOption("stop") )
                log.debug("stop flag detected");
            else if( cli.hasOption("status") )
                log.debug("status flag detected");
            else
                log.debug("no flag detected");
        }catch (ParseException pe){
            fatal(pe.getMessage(), pe);
        }

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

        // integrated with Google authentication
        try {
            GoogleAuthenticator.getInstance().getDriveService();
        } catch (IOException | GeneralSecurityException  e) {
            fatal(e.getMessage(), e);
        }

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
