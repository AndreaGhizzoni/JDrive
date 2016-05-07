package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.applicationutil.Locker;
import it.hackcaffebabe.applicationutil.Util;
import it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.fs.DetectedEvent;
import it.hackcaffebabe.jdrive.fs.watcher.Watcher;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JDrive Application Launcher
 */
public class Launcher {
    private static Logger log = LogManager.getLogger();

    private static CommandLine ARGS_CLI;
    private static Options ARGS_OPTIONS = new Options();

    public static void main( String... args ){
        checkCLIArgs(args);

        boolean startFlag = ARGS_CLI.hasOption("start");
        boolean stopFlag = ARGS_CLI.hasOption("stop");
        boolean statusFlag = ARGS_CLI.hasOption("status");
        boolean helpFlag = ARGS_CLI.hasOption("help");
        boolean noFlag = startFlag || stopFlag || statusFlag || helpFlag;

        if( helpFlag || !noFlag ) {
            HelpFormatter h = new HelpFormatter();
            h.printHelp("<jar> [OPTIONS]\n" +
                        "Where OPTIONS are listed below:", ARGS_OPTIONS);
        }

        long pid = -1;
        try{
            pid = new Locker("JDriveApplication").checkLock();
        }catch (IOException ioe){
            fatal(ioe.getMessage(), ioe);
        }

        boolean isAlreadyRunning = pid != Util.getProcessID();
        if( isAlreadyRunning ){
            if( statusFlag ){
                System.out.println("TODO: checking JDrive status..");
            }else if( stopFlag ){
//                System.out.println("TODO: stopping jDrive..");
                stopJDrive(pid);
            }else if( startFlag ){
                System.out.println("JDrive already running. Use -status");
            }
        }else{
            if( statusFlag || stopFlag ){
                System.out.println("JDrive not running. Use -start");
            }else if( startFlag ){
                startJDrive();
            }
        }
    }

//==============================================================================
//  UTILITY METHODS
//==============================================================================
    // more options @ https://goo.gl/4zOb8V
    private static void checkCLIArgs(String...args){
         try{
             ARGS_OPTIONS.addOption("status", false, "check JDrive status");
             ARGS_OPTIONS.addOption("start", false, "start JDrive");
             ARGS_OPTIONS.addOption("stop", false, "stop JDrive");
             ARGS_OPTIONS.addOption("help", false, "print argument usage");
             ARGS_CLI = new DefaultParser().parse(ARGS_OPTIONS, args);
         }catch (ParseException pe){
             System.out.println(pe.getMessage());
             System.exit(1);
         }
    }

    /* start application main flow */
    private static void startJDrive(){
        try{
            log.info("JDrive Application Starting.");
            log.debug("pid: "+Util.getProcessID());
            PathsUtil.createApplicationHomeDirectory();
            log.info("JDrive Home directory created/detected in: "+PathsUtil.APP_HOME);
        }catch( IOException ioE ){
            fatal(ioE.getMessage(), ioE);
        }

        boolean cfgOK = Configurator.getInstance().load();
        if( !cfgOK ) {
            fatal("Configurator Error. Program Exit.", null);
        }

        // integrated with Google authentication
        try {
            GoogleAuthenticator.getInstance().getDriveService();
        } catch (IOException | GeneralSecurityException | InterruptedException e) {
            fatal(e.getMessage(), e);
        }

        try{
            // process that will start from main application
            final Watcher w = Watcher.getInstance();

            // add a shutdown hook to close all the process above properly
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("JDrive closing procedure...");
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

    private static void stopJDrive( long pid ){
        try {
            log.info("Stopping JDrive from command line detected.");
            Runtime.getRuntime().exec("kill -15 "+pid);
        } catch (IOException e) {
            fatal(e.getMessage(), e);
        }
    }

    // this method write a fatal message into log file and kill the program
    private static void fatal(String msg, Throwable t){
        log.fatal(msg, t);
        System.exit(-1);
    }
}
