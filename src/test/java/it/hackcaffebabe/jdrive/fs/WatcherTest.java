package it.hackcaffebabe.jdrive.fs;

import it.hackcaffebabe.jdrive.cfg.Configurator;
import it.hackcaffebabe.jdrive.util.PathsUtil;
import org.junit.Assert;
import org.junit.Test;
import java.io.*;

/**
 * Test case for {@link it.hackcaffebabe.jdrive.fs.Watcher}
 */
public class WatcherTest
{
    @Test
    public void testWatcher(){
        buildWD();
        Configurator.getInstance().load();

        Watcher w = retrieveTheWatcher();
        if(w == null)
            Assert.fail("Fail to retrieve the Watcher.");
        Thread tw = new Thread(w);
        tw.start();

//        Path base = Paths.get((String)Configurator.getInstance().get("base"));
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    public void buildWD(){
        try {
            PathsUtil.buildWorkingDirectory();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    public Watcher retrieveTheWatcher(){
        try {
            return Watcher.getInstance();
        } catch (IOException e) {
            return null;
        }
    }

//    public void spawnFolder( File parent, int n ){
//        String b = "folder%d";
//        for(int i=0; i<n; i++){
//            if(!new File(parent, String.format(b, i)).mkdirs())
//                Assert.fail("Fail to spawn folders.");
//        }
//    }

//    public void fillFolder( File f, int n ){
//        BufferedWriter bw;
//        FileWriter fw;
//        String b = "f%d.txt";
//        for(int i=0; i<n; i++){
//            try {
//                fw = new FileWriter(new File(f, String.format(b, i)));
//                bw = new BufferedWriter(fw);
//
//                bw.append("Hello Im a new File\n");
//
//                bw.flush();
//                bw.close();
//                fw.close();
//            } catch (IOException e) {
//                Assert.fail(e.getMessage());
//            }
//        }
//    }
//
//    public void cleanBase( File b ){
//        try{
//            for(File i : b.listFiles()){
//               if(i.isDirectory())
//                   cleanBase(i);
//               if(!i.delete())
//                   throw new IOException("Fail to delete some files in cleanBase");
//            }
//        }catch (IOException e){
//            Assert.fail(e.getMessage());
//        }
//    }
}
