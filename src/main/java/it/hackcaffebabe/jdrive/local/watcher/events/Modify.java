package it.hackcaffebabe.jdrive.local.watcher.events;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * This class represents a Modify event associated to a file path.
 */
public class Modify extends WatcherEvent
{
    /**
     * Instance a Modify event associated to a given file path.
     * @param file {@link java.nio.file.Path} the file path of Modify event.
     */
    public Modify( Path file ) { this( file, "Modification event detected." ); }

    /**
     * Instance a Modify event associated to a given file path and custom human
     * readable message.
     * @param file {@link java.nio.file.Path} the file path of Modify event.
     * @param message {@link java.lang.String} a human readable message.
     */
    public Modify( Path file, String message ) { super( file, message ); }

    @Override
    public WatchEvent.Kind Convert() {
        return StandardWatchEventKinds.ENTRY_MODIFY;
    }

    @Override
    public String toString(){
        return String.format( super.toString(), "modify" );
    }
}
