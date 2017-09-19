package it.hackcaffebabe.jdrive.local.watcher.events;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * This class represents a Delete event associated to a file path.
 */
public class Delete extends WatcherEvent
{
    /**
     * Instance a Delete event associated to a given file path.
     * @param file {@link java.nio.file.Path} the file path of Delete event.
     */
    public Delete( Path file ) { this( file, "Delete event detected." ); }

    /**
     * Instance a Delete event associated to a given file path and custom human
     * readable message.
     * @param file {@link java.nio.file.Path} the file path of Delete event.
     * @param message {@link java.lang.String} a human readable message.
     */
    public Delete( Path file, String message ) { super( file, message ); }

    @Override
    public WatchEvent.Kind Convert() {
        return StandardWatchEventKinds.ENTRY_DELETE;
    }

    @Override
    public String toString(){
        return String.format( super.toString(), "delete" );
    }
}
