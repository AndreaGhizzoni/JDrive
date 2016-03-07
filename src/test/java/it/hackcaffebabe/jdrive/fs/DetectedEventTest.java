package it.hackcaffebabe.jdrive.fs;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Date;

/**
 * Test for class {@link DetectedEvent}
 */
public class DetectedEventTest {

    @Test
    public void testDetectedObject(){
        DetectedEvent d = new DetectedEvent();

        // test watch event kind
        WatchEvent.Kind k;
        d.setKind( null );
        Assert.assertEquals("Expecting null from getKind() if setKind(null)",
                null, d.getKind());

        d.setKind( StandardWatchEventKinds.OVERFLOW );
        Assert.assertEquals("Expecting null from getKind() if setKind(OVERFLOW)",
                null, d.getKind());

        k = StandardWatchEventKinds.ENTRY_CREATE;
        d.setKind( k );
        Assert.assertEquals("Expecting kind ENTRY_CREATE from getKind() if " +
                        "setKind(ENTRY_CREATE)", k, d.getKind());

        k = StandardWatchEventKinds.ENTRY_MODIFY;
        d.setKind( k );
        Assert.assertEquals("Expecting kind ENTRY_MODIFY from getKind() if " +
                        "setKind(ENTRY_MODIFY)", k, d.getKind());

        k = StandardWatchEventKinds.ENTRY_DELETE;
        d.setKind( k );
        Assert.assertEquals("Expecting kind ENTRY_DELETE from getKind() if " +
                        "setKind(ENTRY_DELETE)", k, d.getKind());

        // test file path
        String s; Path p; File f;
        d.setDetectedObject("");
        Assert.assertTrue("Expecting null from getPath() if setDetectedObject(\"\")",
                d.getPath() == null);

        s = "/tmp";
        d.setDetectedObject(s);
        Assert.assertEquals("Expecting /tmp from getPathAsString() if setDetectedObject(\"/tmp\")",
                s, d.getPathAsString());

        p = Paths.get(s);
        d.setDetectedObject(p);
        Assert.assertEquals("Expecting /tmp from getPath() if setDetectedObject(\"/tmp\")",
                p, d.getPath() );

        f = new File(s);
        d.setDetectedObject(f);
        Assert.assertEquals("Expecting /tmp from getFile() if setDetectedObject(\"/tmp\")",
                f, d.getFile() );

        // test last modify
        long l = 0L;
        d.setLastModify(l);
        Assert.assertTrue("Expecting -1L from getLastModify() if setLastModify(0L)",
                d.getLastModify() == -1L);

        l = new Date().getTime();
        d.setLastModify(l);
        Assert.assertTrue("Expecting the long did I set from getLastModify()",
                d.getLastModify() == l);

    }
}
