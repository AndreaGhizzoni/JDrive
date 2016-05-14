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
    public void testDetectedEventStatus(){
        boolean mustBeTrue = new DetectedEvent().isEmpty();
        Assert.assertTrue("Expecting empty DetectedEvent.", mustBeTrue);

        // test creation event
        DetectedEvent creation = new DetectedEvent(
                ENTRY_CREATE,
                testPath,
                "This is a creation event of test path"
        );
        boolean isCreation = creation.isEventCreate();
        Assert.assertTrue(
                "Expecting true from isEventCreate() from a Creation event.",
                isCreation
        );

        // test modification event
        DetectedEvent modification = new DetectedEvent(
                ENTRY_MODIFY,
                testPath,
                "This is a modification event of test path"
        );
        boolean isModification = modification.isEventModify();
        Assert.assertTrue(
                "Expecting true from isEventModify() from a Modification event.",
                isModification
        );

        // test delete event
        DetectedEvent delete = new DetectedEvent(
                ENTRY_DELETE,
                testPath,
                "This is a Delete event of test path"
        );
        boolean isDelete = delete.isEventDelete();
        Assert.assertTrue(
                "Expecting true from isEventDelete() from a Delete event.",
                isDelete
        );

        // test error event
        DetectedEvent e1 = new DetectedEvent(
                null, null, "This is a error event"
        );
        DetectedEvent e2 = new DetectedEvent(
                null, null, null
        );
        DetectedEvent e3 = new DetectedEvent(
                null, testPath, "This is a error event"
        );
        DetectedEvent e4 = new DetectedEvent(
                null, testPath, null
        );
        boolean isError = e1.containError() && e2.containError() &&
                          e3.containError() && e4.containError();
        Assert.assertTrue(
                "Expecting true from containsError() from a error event.",
                isError
        );
    }
}
