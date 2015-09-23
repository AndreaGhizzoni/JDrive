package it.hackcaffebabe.jdrive.fs;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
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
        Path watchedDir = Paths.get((String) Configurator.getInstance()
                .get(Keys.WATCHED_DIR));
        try {
            Files.walkFileTree(watchedDir, new ServiceAdder());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

//==============================================================================
//  INNER CLASS
//==============================================================================
    /* inner class that walk down a given path and cache all the files */
    private class ServiceAdder extends SimpleFileVisitor<Path> {
//        private WatcherCache cache;
//        public ServiceAdder(){
//            try {
//                cache = WatcherCache.getInstnace();
//            } catch (IOException e) {
//                log.error(e.getMessage(), e);
//            }
//        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes a)
                throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
