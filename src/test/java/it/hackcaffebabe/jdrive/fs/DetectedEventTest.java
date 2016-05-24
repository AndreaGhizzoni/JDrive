package it.hackcaffebabe.jdrive.fs;

import org.junit.Assert;
import org.junit.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Test for class {@link DetectedEvent}
 */
public class DetectedEventTest {
    private final Path testPath = Paths.get(
            System.getProperty("java.io.tmpdir")
    );

    @Test
    public void testPassingNullAsKind(){
        String someMsg = "some msg";
        DetectedEvent d = new DetectedEvent( null, testPath, someMsg );
        Assert.assertNull(
                "Expected null from passing null as kind",
                d.getKind()
        );
        Assert.assertTrue(
                "Expected return the same file path as set",
                d.getFile().equals(testPath.toFile().getAbsolutePath())
        );
        Assert.assertTrue(
                "Expected d.containError() returns true",
                d.containError()
        );
        Assert.assertTrue(
                "Expected return the same message as set",
                d.getMessage().equals(someMsg)
        );
    }

    @Test
    public void testPassingNullAsFile(){
        String someMsg = "some msg";
        DetectedEvent d = new DetectedEvent( ENTRY_CREATE, null, someMsg );
        Assert.assertTrue(
                "Expect return the same kind as set",
                d.getKind() == ENTRY_CREATE
        );
        Assert.assertNull(
                "Expected null from passing null as file path",
                d.getFile()
        );
        Assert.assertTrue(
                "Expected negative number as last modify from null file path",
                d.getLastModify() < 0L);
        Assert.assertTrue("Expected return the same message as set",
                d.getMessage().equals(someMsg));
    }

    @Test
    public void testPassingNullAsMessage(){
        DetectedEvent d = new DetectedEvent( ENTRY_CREATE, testPath, null );
        Assert.assertTrue(
                "Expect return the same kind as set",
                d.getKind() == ENTRY_CREATE
        );
        Assert.assertTrue(
                "Expected return the same file path as set",
                d.getFile().equals(testPath.toFile().getAbsolutePath())
        );
        Assert.assertNotNull(
                "Expected default message from given null message",
                d.getMessage()
        );
    }

    @Test
    public void testMessageUsingWrongConstructor(){
        DetectedEvent e5 = new DetectedEvent( null, null );
        Assert.assertNotNull(
                "Expected default error message from using wrong constructor " +
                        "for errors",
                e5.getMessage()
        );
    }

    @Test
    public void testEmptyStatus(){
        boolean mustBeTrue = new DetectedEvent().isEmpty();
        Assert.assertTrue("Expecting empty DetectedEvent.", mustBeTrue);
    }

    @Test
    public void testCreationStatus(){
        DetectedEvent creationFirst = new DetectedEvent(
                ENTRY_CREATE,
                testPath,
                "This is a creation event of test path"
        );
        DetectedEvent creationSecond = new DetectedEvent(
                ENTRY_CREATE,
                testPath
        );
        boolean isCreationFirst = creationFirst.isEventCreate();
        boolean isCreationSecond = creationSecond.isEventCreate();
        Assert.assertTrue(
                "Expecting true from isEventCreate() from a Creation event " +
                        "with full parameters constructor.",
                isCreationFirst
        );
        Assert.assertTrue(
                "Expecting true from isEventCreate() from a Creation event" +
                        " with less parameters constructor.",
                isCreationSecond
        );
    }

    @Test
    public void testModificationStatus(){
        DetectedEvent modificationFirst = new DetectedEvent(
                ENTRY_MODIFY,
                testPath,
                "This is a modification event of test path"
        );
        DetectedEvent modificationSecond = new DetectedEvent(
                ENTRY_MODIFY,
                testPath
        );
        boolean isModificationFirst = modificationFirst.isEventModify();
        boolean isModificationSecond = modificationSecond.isEventModify();
        Assert.assertTrue(
                "Expecting true from isEventModify() from a Modification " +
                        "event with full parameters constructor.",
                isModificationFirst
        );
        Assert.assertTrue(
                "Expecting true from isEventModify() from a Modification " +
                        "event with less parameters constructor.",
                isModificationSecond
        );
    }

    @Test
    public void testDeleteStatus(){
        DetectedEvent deleteFirst = new DetectedEvent(
                ENTRY_DELETE,
                testPath,
                "This is a Delete event of test path"
        );
        DetectedEvent deleteSecond = new DetectedEvent(
                ENTRY_DELETE,
                testPath
        );
        boolean isDeleteFirst = deleteFirst.isEventDelete();
        boolean isDeleteSecond = deleteSecond.isEventDelete();
        Assert.assertTrue(
                "Expecting true from isEventDelete() from a Delete event with" +
                        "full parameters constructor.",
                isDeleteFirst
        );
        Assert.assertTrue(
                "Expecting true from isEventDelete() from a Delete event with" +
                        "less parameters constructor.",
                isDeleteSecond
        );
    }

    @Test
    public void testErrorStatus(){
        DetectedEvent e1 = new DetectedEvent(
                null, null, "This is a error event"
        );
        Assert.assertTrue(
                "Expecting that DetectedEvent contains an error from passing " +
                        "(null, null, \"string\") as constructor parameters",
                e1.containError()
        );

        DetectedEvent e2 = new DetectedEvent(
                null, null, null
        );
        Assert.assertTrue(
                "Expecting that DetectedEvent contains an error from passing " +
                        "(null, null, null) as constructor parameters",
                e2.containError()
        );

        DetectedEvent e3 = new DetectedEvent(
                null, testPath, "This is a error event"
        );
        Assert.assertTrue(
                "Expecting that DetectedEvent contains an error from passing " +
                        "(null, somePath, \"string\") as constructor parameters",
               e3.containError()
        );

        DetectedEvent e4 = new DetectedEvent(
                null, testPath, null
        );
        Assert.assertTrue(
                "Expecting that DetectedEvent contains an error from passing " +
                        "(null, somePath, null) as constructor parameters",
                e4.containError()
        );
    }
}
