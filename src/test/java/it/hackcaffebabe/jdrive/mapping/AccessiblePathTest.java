package it.hackcaffebabe.jdrive.mapping;

import it.hackcaffebabe.jdrive.Constants;
import it.hackcaffebabe.jdrive.cfg.Configurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AccessiblePathTest
{
    public class TestObj {
        String path;
        boolean accessible;
        TestObj( String path, boolean accessible ){
            this.path = path;
            this.accessible = accessible;
        }
    }

    private static String SEP = System.getProperty("file.separator");
    private static String BASE = System.getProperty("user.home")+SEP;
    private static String BASE_G = BASE+SEP+"Google Drive"+SEP;

    @Before
    public void setUp(){
       try{
           Configurator.setup( Paths.get( Constants.APP_PROPERTIES_FILE ) );
       } catch (Exception e) {
           Assert.fail(e.getMessage());
       }
    }

    @Test
    public void test_getFields() {
        List<TestObj> tableTest = new ArrayList<TestObj>(){{
            add( new TestObj("", true) );
            add( new TestObj(null, true) );
            add( new TestObj(BASE_G+"folder", true) );
            add( new TestObj(BASE_G+"file.txt", true) );
            add( new TestObj(BASE_G+"folder"+SEP+"file.txt", true) );

            add( new TestObj("", false) );
            add( new TestObj(null, false) );
            add( new TestObj(BASE_G+"folder", false) );
            add( new TestObj(BASE_G+"file.txt", false) );
            add( new TestObj(BASE_G+"folder"+SEP+"file.txt", false) );
        }};

        tableTest.forEach( testObj -> {
            AccessiblePath complete = new AccessiblePath(
                testObj.path, testObj.accessible
            );
            testSingleAccessiblePath( testObj.path, testObj.accessible, complete );

            AccessiblePath simple = new AccessiblePath( testObj.path );
            testSingleAccessiblePath( testObj.path, true, simple );
        });
    }

    private void testSingleAccessiblePath( String path, boolean accessible,
                                          AccessiblePath accessiblePath ) {
        Assert.assertTrue(
            "After setting an accessible level I expect that isAccessible return" +
                    "that accessible level",
            accessiblePath.isAccessible() == accessible
        );

        Assert.assertEquals(
            "After setting a path I expect that getPath return the same path",
            path == null ? "" : path,
            accessiblePath.getPath()
        );
    }
}
