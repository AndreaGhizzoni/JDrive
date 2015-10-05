package it.hackcaffebabe.jdrive.fs.watcher;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.cfg.Keys;
import it.hackcaffebabe.jdrive.fs.DetectedObject;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.junit.Assert;
import org.junit.Test;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Test case for {@link it.hackcaffebabe.jdrive.fs.watcher.Watcher}
 */
public class WatcherTest
{
    LinkedBlockingQueue<DetectedObject> lbq = new LinkedBlockingQueue<DetectedObject>();

    @Test
    public void testWatcher(){
        buildWD();
        buildConfigurator();
        Path wd = Paths.get((String)Configurator.getInstance().get(Keys.WATCHED_DIR));

        Watcher w = null;
        try {
            w = Watcher.getInstance();
        } catch (IOException e) {
            Assert.fail("Fail to retrieve the Watcher. "+e.getMessage());
        }

        w.setDispatchingQueue(lbq);
        Thread t = new Thread(w);
        t.start();

        // spawning folder test
        spawnFolder( wd.toFile(), 10 );
        listenFromQueue(
            Arrays.asList(StandardWatchEventKinds.ENTRY_CREATE),
            "After spawn some folders I expect ENTRY_CREATE"
        );

        // creation file and modify
        BufferedWriter bw;
        FileWriter fw;
        String b = "f%d.txt";
        int n = 10;
        for(int i=0; i<n; i++){
            try {
                fw = new FileWriter(new File(wd.toFile(), String.format(b, i)));
                bw = new BufferedWriter(fw);

                bw.append("Hello Im a new File\n");
                bw.flush();
                bw.close();
                fw.close();

                listenFromQueue(
                    Arrays.asList(StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY),
                    "After spawn a file and modify it I expect ENTRY_MODIFY and" +
                            "ENTRY_CREATE"
                );
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }

        t.interrupt();
        cleanWD(wd.toFile());
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    public void listenFromQueue( List<WatchEvent.Kind<Path>> expected, String msg ){
        DetectedObject detObj;
        while( !lbq.isEmpty() ){
            try {
                detObj = lbq.take();
                Assert.assertTrue(msg, expected.contains(detObj.getKind()));
            } catch (InterruptedException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    public void buildWD(){
        try {
            PathsUtil.createApplicationHomeDirectory();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public void buildConfigurator(){
       try{
           Configurator.getInstance().load();
       } catch (Exception e) {
           Assert.fail(e.getMessage());
       }
    }

    public void spawnFolder( File parent, int n ){
        String b = "folder%d";
        for(int i=0; i<n; i++){
            if(!new File(parent, String.format(b, i)).mkdirs())
                Assert.fail("Fail to spawn folders.");
        }
    }

    public void cleanWD(File b){
        try{
            for(File i : b.listFiles()){
               if(i.isDirectory())
                   cleanWD(i);
               if(!i.delete())
                   throw new IOException("Fail to delete some files in cleanWD");
            }
        }catch (IOException e){
            Assert.fail(e.getMessage());
        }catch (NullPointerException eN){
            Assert.fail(eN.getMessage());
        }
    }
}
