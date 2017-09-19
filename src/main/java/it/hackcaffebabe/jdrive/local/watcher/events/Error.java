package it.hackcaffebabe.jdrive.local.watcher.events;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * This class represents an Error event with a human readable message that
 * explain what happened.
 */
public class Error extends WatcherEvent
{
    /**
     * Instance an Error event with associated human readable message.
     * @param message {@link java.lang.String} a human readable message to
     *                                         explain the error.
     */
    public Error( String message ){ this( null, message ); }

    /**
     * Instance a Error event associated to a given file path and custom human
     * readable message.
     * @param file {@link java.nio.file.Path} the file path of Error event.
     * @param message {@link java.lang.String} a human readable message.
     */
    public Error( Path file, String message ){ super( file, message ); }

    @Override
    public WatchEvent.Kind Convert() { return null; }

    @Override
    public String toString(){
        return String.format( super.toString(), "error" );
    }
}
