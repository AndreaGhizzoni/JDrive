package it.hackcaffebabe.jdrive.remote.google;

import com.google.api.services.drive.model.File;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for {@link it.hackcaffebabe.jdrive.remote.google.Mapper}
 */
public class MapperTest
{
    @Test
    public void test_PutGetRemovePathOnly(){
        List<Path> paths = new ArrayList<Path>(){{
            add(Paths.get("/tmp/file1"));
            add(Paths.get("/tmp"));
            add(Paths.get("/not_a_folder"));
        }};

        Mapper mapper = Mapper.getInstance();
        paths.forEach( path -> {
            mapper.put( path );
            Assert.assertTrue(
                "Putting a single path without any restriction must be "+
                    "accessible == true",
                mapper.isAccessible( path )
            );
            Assert.assertNull(
                "Putting a single path without any remote file, must get "+
                    "a null object from get()",
                mapper.get( path )
            );

            File remoteFile = mapper.remove( path.toString() );
            Assert.assertNull(
                "Returned file from simple path must be null",
                remoteFile
            );
            Assert.assertFalse(
                "Removing path must resulting in accessible == false",
                mapper.isAccessible( path )
            );
            Assert.assertNull(
                "Removing path must resulting in a null object from get()",
                mapper.get( path )
            );
        });
    }

}
