package it.hackcaffebabe.jdrive.remote.google;

import static it.hackcaffebabe.jdrive.Launcher.setPidToThreadContext;

import it.hackcaffebabe.jdrive.events.Event;
import it.hackcaffebabe.jdrive.remote.watcher.events.Download;
import it.hackcaffebabe.jdrive.remote.watcher.events.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * TODO add doc
 */
public class Downloader implements Runnable
{
    private static final Logger log = LogManager.getLogger();
    private LinkedBlockingQueue<Event> eventsQueue;

    /**
     * TODO add doc
     * @param eventsQueue
     */
    public Downloader( LinkedBlockingQueue<Event> eventsQueue ) {
        this.eventsQueue = eventsQueue;
        log.info("Downloader ready to start.");
    }

    @Override
    public void run() {
        setPidToThreadContext();
        log.info("Downloader started.");

        try{
            boolean keepRunning = true;
            Event detectEvent;
            while ( keepRunning ){
                detectEvent = this.eventsQueue.take();
                if( detectEvent instanceof Download ){
                    log.debug( ((Download)detectEvent).toString() );

                }else if( detectEvent instanceof Error ){
                    log.debug( ((Error)detectEvent).toString() );
                    keepRunning = false;
                }
            }

            log.info("Downloader closing.");
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }
}