package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.applicationutil.Locker;
import it.hackcaffebabe.applicationutil.Util;
import it.hackcaffebabe.jdrive.action.ActionClient;
import it.hackcaffebabe.jdrive.auth.google.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.fs.watcher.Watcher;
import it.hackcaffebabe.jdrive.fs.watcher.events.Error;
import it.hackcaffebabe.jdrive.fs.watcher.events.WatcherEvent;
import it.hackcaffebabe.jdrive.action.ActionServer;
import it.hackcaffebabe.jdrive.action.Message;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JDrive Application Launcher
 */
public class Launcher
{
    private static Logger log = LogManager.getLogger();

    private static CommandLine CLI_PARSER;
    private static Options FLAGS = new Options();

    public static void main( String... args ){
        populateOptionsAndCLIParser(args);

        boolean startFlag = CLI_PARSER.hasOption("start");
        boolean stopFlag = CLI_PARSER.hasOption("stop");
        boolean statusFlag = CLI_PARSER.hasOption("status");
        boolean helpFlag = CLI_PARSER.hasOption("help");
        boolean versionFlag = CLI_PARSER.hasOption("version");
        boolean noFlag = !(startFlag || stopFlag || statusFlag || helpFlag ||
                         versionFlag);

        if( helpFlag || noFlag ) {
            System.out.println( "JDrive version: " + Constants.VERSION );
            new HelpFormatter().printHelp(
                "<jar> [OPTIONS]\nWhere OPTIONS are listed below:",
                FLAGS
            );
        }

        if( versionFlag ){
            System.out.println( Constants.VERSION );
            System.exit(0);
        }

        long pid = -1;
        try{
            pid = new Locker("JDriveApplication").checkLock();
        }catch (IOException ioe){
            fatalAndQuit(ioe.getMessage(), ioe);
        }

        boolean isAlreadyRunning = ( pid != Util.getProcessID() );
        if( isAlreadyRunning ){
            if( statusFlag ){
                System.out.println("TODO: checking JDrive status..");
            }else if( stopFlag ){
                stopJDrive();
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
    private static void populateOptionsAndCLIParser( String... args ){
         try{
             FLAGS.addOption("status", false, "check JDrive status");
             FLAGS.addOption("start", false, "start JDrive");
             FLAGS.addOption("stop", false, "stop JDrive");
             FLAGS.addOption("help", false, "print argument usage");
             FLAGS.addOption("version", false, "print current version");
             CLI_PARSER = new DefaultParser().parse(FLAGS, args);
         }catch (ParseException pe){
             fatalAndQuit(pe.getMessage(), pe);
         }
    }

    private static void startJDrive(){
        log.info("Starting JDrive application.");
        log.debug("pid: "+Util.getProcessID());
        createApplicationHomeDirectoryOrFail();
        setupConfiguratorOrFail();
        authenticateWithGoogleOrFail();

        try{
            final Watcher watcher = Watcher.getInstance();
            LinkedBlockingQueue<WatcherEvent> lbq = new LinkedBlockingQueue<>();
            watcher.setDispatchingQueue(lbq);

            ActionServer actionServer = new ActionServer();
            actionServer.putAction(
                Message.QUIT,
                () -> {
                    log.info("JDrive closing procedure...");
                    watcher.startClosingProcedure();
                    return true;
                }
            );
            new Thread( actionServer, "CloserListener" ).start();
            new Thread( watcher, "Watcher" ).start();

            WatcherEvent detObj;
            boolean keepRunning = true;
            while(keepRunning){
                detObj = lbq.take();
                if( detObj instanceof Error ){
                    log.info("Error message from Watcher: "+detObj.getMessage());
                    keepRunning = false;
                }
            }
        }catch( Exception ex ){
            fatalAndQuit(ex.getMessage(), ex);
        }
    }

    private static void createApplicationHomeDirectoryOrFail() {
        try{
            Files.createDirectories( Paths.get(Constants.APP_HOME) );
            log.info(
                "JDrive Home directory created/detected in: "+Constants.APP_HOME
            );
        }catch (IOException ioE){
            fatalAndQuit(ioE.getMessage(), ioE);
        }
    }

    private static void setupConfiguratorOrFail(){
        try{
            Configurator.setup(
                Paths.get( Constants.APP_PROPERTIES_FILE )
            );
        }catch (Exception e){
            fatalAndQuit("Configurator Error. Program Exit.", e);
        }
    }

    private static void authenticateWithGoogleOrFail(){
        try {
            GoogleAuthenticator.getInstance().authenticate();
        } catch (IOException | GeneralSecurityException e) {
            fatalAndQuit(e.getMessage(), e);
        }
    }

    private static void stopJDrive(){
        try {
            ActionClient.sendQuitRequest();
        } catch (IOException e) {
            log.error( e.getMessage() );
        }
    }

    private static void fatalAndQuit(String msg, Throwable t){
        log.fatal(msg, t);
        System.exit(1);
    }
}
