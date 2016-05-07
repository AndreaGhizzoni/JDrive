package it.hackcaffebabe.jdrive.z_testingstuff;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class Test {
    public static void main( String... args ){
        final LinkedBlockingQueue<String> q = new LinkedBlockingQueue<String>();

        try {
            Producer p = new Producer(q);
            p.start();

            DownloadManager d =  new DownloadManager(q);
            d.start();
            Thread.sleep(3000);
            d.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class Producer extends Thread {
        private static final Logger log = LogManager.getLogger();
        private final Random rand = new Random();

        private boolean running;
        private final LinkedBlockingQueue<String> queue;

        public Producer(LinkedBlockingQueue queue){
            this.running = false;
            this.queue = queue;
        }

        @Override
        public void run(){
           log.info("Started. generating stuff to sand...");
            this.running = true;

            while(this.isRunning()){
                try {
                    String produced = produce();
                    log.info("String produced: "+produced);
                    this.queue.put(produced);

                    this.running = false;
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    log.info("Stop.");
                    this.running = false;
                }
            }

            log.info("Stop.");
        }

        private String produce(){
            log.entry();
            byte[] b = new byte[10];
            rand.nextBytes(b);
            return new String(b);
        }

        public void terminate(){
            this.running = false;
        }

        public boolean isRunning(){
            return this.running;
        }
    }
}
