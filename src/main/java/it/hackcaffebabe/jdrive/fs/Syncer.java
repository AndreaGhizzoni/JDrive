package it.hackcaffebabe.jdrive.fs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public final class Syncer {
    private static final Logger log = LogManager.getLogger(
        Syncer.class.getSimpleName()
    );

    private LinkedBlockingQueue<DetectedObject> dispatchingQueue;

    /**
     *
     * @param dispatchingQueue
     * @throws IllegalArgumentException
     */
    public Syncer( LinkedBlockingQueue<DetectedObject> dispatchingQueue )
            throws IllegalArgumentException{
        if( dispatchingQueue == null ){
            IllegalArgumentException up =
                    new IllegalArgumentException("Dispatching Queue is null.");
            log.error(up.getMessage());
            throw up;
        }
        this.dispatchingQueue = dispatchingQueue;
    }

    public void check(){

    }

}
