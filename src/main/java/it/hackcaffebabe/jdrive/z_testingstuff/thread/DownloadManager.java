package it.hackcaffebabe.jdrive.z_testingstuff.thread;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * Simple thread class to study how wait() and notifyAll() methods works
 */
public class DownloadManager extends Thread {
    private static final Logger log = LogManager.getLogger(
            DownloadManager.class.getSimpleName()
    );

    private boolean running;
    private final BlockingQueue<String> queue;

    public DownloadManager(BlockingQueue<String> queue){
        this.running = false;
        this.queue = queue;
    }

    @Override
    public void run() {
        log.info("Started. Wait for the queue");
        this.setRunning(true);

        String msg;
        while(this.isRunning()){
            try {
                msg = this.queue.take();
                processIt(msg);
            } catch (InterruptedException e) {
                log.info("Interrupted Exception calling...");
                //TODO IMPORTANT! check if queue is empty! if not flush it.
                this.setRunning(false);
            }
        }
    }

    public void processIt(String it){
        log.info(String.format("I have received: %s", it));
    }

//==============================================================================
// SETTER
//==============================================================================
    private void setRunning( boolean b ){
        this.running = b;
    }

//==============================================================================
// GETTER
//==============================================================================
    public boolean isRunning(){
        return this.running;
    }
}
