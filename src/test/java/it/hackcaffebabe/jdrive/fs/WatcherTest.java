package it.hackcaffebabe.jdrive.fs;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Test case for {@link it.hackcaffebabe.jdrive.fs.Watcher}
 */
public class WatcherTest
{
    @Test
    public void testWatcher(){
        Path base = getBasePath();
        if(base == null)
            Assert.fail("Fail to get the base path.");

        try{// clean up test folder
            cleanBase(base.toFile());
        }catch (IOException e){
            Assert.fail(e.getMessage());
        }

        Watcher w = retrieveTheWatcher(base);
        if(w == null)
            Assert.fail("Fail to retrieve the Watcher.");

        File newSubFolder = createSubFolder(base.toFile(), "f1");
        if(newSubFolder == null)
            Assert.fail("Fail to create sub-folder of base path.");

//        if(w.getNumberFolderWatched() == 2)
//            Assert.fail("Number of folder watched must be 2.");
        if(!w.isFileWatched(newSubFolder.toPath()))
            Assert.fail("Fail to watch new sub folder.");

        File newFile = createTestFile(newSubFolder, "test");
        if(newFile == null)
            Assert.fail("Fail to create test file into sub-folder.");

        if(!w.isFileWatched(newSubFolder.toPath()))
            Assert.fail("Fail to watch new file.");
//        if(w.getNumberFolderWatched() == 3)
//            Assert.fail("Number of folder watched must be 3.");
    }

//==============================================================================
//  TEST CASE UTIL METHOD
//==============================================================================
    public Path getBasePath(){
        Path base = WatcherUtil.getBase();
        File f = base.toFile();
        if(!f.exists() && !f.mkdir())
            return null;
        return base;
    }

    public Watcher retrieveTheWatcher(Path base){
        try {
            return Watcher.getInstance(base);
        } catch (IOException e) {
            return null;
        }
    }

    public File createTestFile( File parent, String name){
        File newFile = new File(parent, name);
        try {
            if(!newFile.createNewFile())
                return null;
        } catch (IOException e) {
            return null;
        }
        return newFile;
    }

    public File createSubFolder( File parent, String name ){
        File newFile = new File(parent, name);
        if(!newFile.mkdir())
           return null;
        return newFile;
    }

    public void cleanBase( File b ) throws IOException{
        for(File i : b.listFiles()){
           if(i.isDirectory())
               cleanBase(i);
           if(!i.delete())
               throw new IOException("Fail to delete some files in cleanBase");
        }
    }
}
