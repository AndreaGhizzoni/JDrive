package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.applicationutil.Locker;
import it.hackcaffebabe.applicationutil.Util;
import it.hackcaffebabe.jdrive.action.ActionClient;
import it.hackcaffebabe.jdrive.remote.google.auth.GoogleAuthenticator;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.fs.watcher.Watcher;
import it.hackcaffebabe.jdrive.fs.watcher.events.*;
import it.hackcaffebabe.jdrive.action.ActionServer;
import it.hackcaffebabe.jdrive.action.Message;
import it.hackcaffebabe.jdrive.remote.google.UpLoader;
import it.hackcaffebabe.jdrive.remote.google.watcher.RemoteWatcher;
import it.hackcaffebabe.jdrive.util.DateUtils;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;

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
    private static Logger log;

    private static CommandLine CLI_PARSER;
    private static Options FLAGS = new Options();

    public static void main( String... args ){
        setLog4jVariablesAndInitialize();
        populateOptionsAndCLIParser(args);

        boolean startFlag = CLI_PARSER.hasOption("start");
        boolean stopFlag = CLI_PARSER.hasOption("stop");
        boolean statusFlag = CLI_PARSER.hasOption("status");
        boolean helpFlag = CLI_PARSER.hasOption("help");
        boolean versionFlag = CLI_PARSER.hasOption("version");
        boolean specsFlag = CLI_PARSER.hasOption("specs");
        boolean noFlag = !(startFlag || stopFlag || statusFlag || helpFlag ||
                         versionFlag || specsFlag);

        if( helpFlag || noFlag ) {
            System.out.println( "JDrive version: " + Constants.VERSION );
            new HelpFormatter().printHelp(
                "<jar> [OPTIONS]\nWhere OPTIONS are listed below:",
                FLAGS
            );
        }

        Constants.CURRENT_PID = Util.getProcessID();
        setPidToThreadContext();
        if( specsFlag ){
            SystemInfo.doLog();
        }

        if( versionFlag ){
            System.out.println( Constants.VERSION );
            System.exit(0);
        }

        Long lockerPID = -1L;
        try{
            lockerPID = new Locker("JDriveApplication").checkLock();
        }catch (IOException ioe){
            fatalAndQuit(ioe.getMessage(), ioe);
        }

        boolean isAlreadyRunning = !Constants.CURRENT_PID.equals(lockerPID);
        if( isAlreadyRunning ){
            if( statusFlag ){
                statusJDrive();
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
    private static void setLog4jVariablesAndInitialize(){
        String date = DateUtils.formatTimestamp(
            System.currentTimeMillis(),
            "yyyy-MM-dd"
        );
        System.setProperty("date", date);

        ((LoggerContext)LogManager.getContext(false)).reconfigure();
        log = LogManager.getLogger();
    }

    public static void setPidToThreadContext() {
        ThreadContext.put("pid", String.valueOf(Constants.CURRENT_PID));
    }

    // more options @ https://goo.gl/4zOb8V
    private static void populateOptionsAndCLIParser( String... args ){
         try{
             FLAGS.addOption("status", false, "check JDrive status");
             FLAGS.addOption("start", false, "start JDrive");
             FLAGS.addOption("stop", false, "stop JDrive");
             FLAGS.addOption("help", false, "print argument usage");
             FLAGS.addOption("version", false, "print current version");
             FLAGS.addOption("specs", false, "log current specs");
             CLI_PARSER = new DefaultParser().parse(FLAGS, args);
         }catch (ParseException pe){
             fatalAndQuit(pe.getMessage(), pe);
         }
    }

    private static void startJDrive(){
        log.info("Starting JDrive application.");
        Status.WATCHER = "Starting JDrive application. ";
        log.debug("pid: "+Util.getProcessID());
        createApplicationHomeDirectoryOrFail();
        setupConfiguratorOrFail();
        Status.WATCHER = "JDrive configured properly.";
        authenticateWithGoogleOrFail();
        Status.WATCHER = "JDrive logged in.";

        try{
            final RemoteWatcher remoteWatcher = RemoteWatcher.getInstance();

            final Watcher watcher = Watcher.getInstance();
            watcher.init();

            LinkedBlockingQueue<WatcherEvent> lbq = new LinkedBlockingQueue<>();
            watcher.setDispatchingQueue(lbq);

            ActionServer actionServer = new ActionServer();
            actionServer.addAction(
                Message.QUIT,
                () -> {
                    log.info("JDrive closing procedure...");
                    watcher.startClosingProcedure();
                    return "JDrive closing procedure done.";
                }
            );
            actionServer.addAction(
                Message.STATUS,
                () -> {
                    log.info("Current status requested: "+Status.WATCHER);
                    return Status.WATCHER;
                }
            );

            new Thread( actionServer, "ActionServer" ).start();
            new Thread( watcher, "Watcher" ).start();
            Thread upLoaderThread = new Thread( new UpLoader(lbq), "Uploader" );
            upLoaderThread.start();
            upLoaderThread.join();
            log.debug("Uploader has been closed, shutting down.");

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
            log.info("stop flag set: proceeding with JDrive closing procedure.");
            ActionClient.sendQuitRequest();
        } catch (IOException e) {
            log.error( e.getMessage(), e );
        }
    }

    private static void statusJDrive(){
        try {
            String currentStatus = ActionClient.sendStatusRequest();
            System.out.println( currentStatus );
        } catch (IOException e) {
            log.error( e.getMessage(), e);
        }
    }

    private static void fatalAndQuit(String msg, Throwable t){
        log.fatal(msg, t);
        System.exit(1);
    }
}
