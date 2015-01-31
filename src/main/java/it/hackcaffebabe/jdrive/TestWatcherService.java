package it.hackcaffebabe.jdrive;

import it.hackcaffebabe.jdrive.fs.Watcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 *
 */
public class TestWatcherService
{
    private static final Logger log = LogManager.getLogger("TestWatcherService");

    public static void main(String...args){
        try {
            Thread t = new Thread(Watcher.getInstance());
            t.start();
//            Thread.sleep(1000);
//            t.interrupt();
        }catch (IOException ioe){
            log.error("IOException Throw:" + ioe.getMessage());
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
