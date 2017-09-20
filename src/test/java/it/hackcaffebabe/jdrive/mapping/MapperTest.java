package it.hackcaffebabe.jdrive.mapping;

import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.mapping.Mapper;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for {@link Mapper}
 */
public class MapperTest
{
    public class TestObj{
        private Path path;
        private boolean accessible;
        private File remoteFile;

        TestObj( Path p, boolean accessible, File remoteFile ){
            this.path = p;
            this.accessible = accessible;
            this.remoteFile = remoteFile;
        }
    }

    private static String SEP = System.getProperty("file.separator");
    private static String BASE = System.getProperty("user.home")+SEP;
    private static String BASE_G = BASE+SEP+"Google Drive"+SEP;

    @Test
    public void test_PutGetRemovePathOnly(){
        List<TestObj> tableTest = new ArrayList<TestObj>(){{
            add( new TestObj(Paths.get(BASE_G), true, null) );
            add( new TestObj(Paths.get(BASE_G+"tmp"), true, null) );
            add( new TestObj(Paths.get(BASE_G+"not_a_folder"), true, null) );
            add( new TestObj(Paths.get(BASE_G+"tmp"+SEP+"file1.txt"), true, null) );

            add( new TestObj(Paths.get(BASE_G), false, null) );
            add( new TestObj(Paths.get(BASE_G+"tmp"), false, null) );
            add( new TestObj(Paths.get(BASE_G+"not_a_folder"), false, null) );
            add( new TestObj(Paths.get(BASE_G+"tmp"+SEP+"file1.txt"), false, null) );
        }};

        Mapper mapper = new Mapper();
        tableTest.forEach( testObj -> {
            mapper.put(
                testObj.path.toString(),
                testObj.accessible,
                testObj.remoteFile
            );
            Assert.assertEquals(
                "After putting a path with accessible="+testObj.accessible+
                    " I expect that mapping.isAccessible returns true",
                testObj.accessible,
                mapper.isAccessible( testObj.path )
            );

            File actualRemoteFile = mapper.get( testObj.path );
            if( testObj.remoteFile != null ){
                Assert.assertEquals(
                    "After get a remote file from path="+testObj.path+" I expect" +
                        " that is equals to remote file previously put",
                    testObj.remoteFile,
                    actualRemoteFile
                );
            }else {
                Assert.assertNull(
                    "After putting null as remote file associated with " +
                        "path="+testObj.path+" I expect that get returns null",
                    actualRemoteFile
                );
            }

            File remoteFile = mapper.remove( testObj.path.toString() );
            if( testObj.remoteFile != null ){
                Assert.assertEquals(
                    "After removing a path I expect that the associated remote " +
                        "file is equals to remote file previously put",
                    testObj.remoteFile,
                    remoteFile
                );
            }else {
                Assert.assertNull(
                    "After removing a path associated with null remote file "+
                        "I expect that remove method returns null",
                    remoteFile
                );
            }
            Assert.assertNull(
                "Once removed a path I expect a null file from get()",
                mapper.get( testObj.path )
            );
            Assert.assertFalse(
                "Once removed a path I expect that isAccessible return false",
                mapper.isAccessible( testObj.path )
            );
        });
    }

}
