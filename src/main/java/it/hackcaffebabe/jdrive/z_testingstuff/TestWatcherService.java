package it.hackcaffebabe.jdrive.z_testingstuff;

import it.hackcaffebabe.jdrive.util.PathsUtil;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.fs.Watcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class TestWatcherService
{
    private static final Logger log = LogManager.getLogger("MyTest");

    public static void main(String...args){
        try {
            PathsUtil.buildWorkingDirectory();
            Configurator.getInstance().load();

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
