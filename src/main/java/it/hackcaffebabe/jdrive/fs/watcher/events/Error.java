package it.hackcaffebabe.jdrive.fs.watcher.events;


import java.nio.file.WatchEvent;

/**
 * TODO add doc
 */
public class Error extends WatcherEvent
{
    /**
     * TODO add doc
     * @param message
     */
    public Error( String message ){ super( null, message ); }

    @Override
    public WatchEvent.Kind Convert() { return null; }
}
