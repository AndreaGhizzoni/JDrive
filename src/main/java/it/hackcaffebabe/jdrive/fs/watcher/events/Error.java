package it.hackcaffebabe.jdrive.fs.watcher.events;

import java.nio.file.WatchEvent;

/**
 * This class represents an Error event with a human readable message that
 * explain what happened.
 */
public class Error extends WatcherEvent
{
    /**
     * Create an Error event with associated human readable message.
     * @param message {@link java.lang.String} a human readable message to
     *                                         explain the error.
     */
    public Error( String message ){ super( null, message ); }

    @Override
    public WatchEvent.Kind Convert() { return null; }
}
