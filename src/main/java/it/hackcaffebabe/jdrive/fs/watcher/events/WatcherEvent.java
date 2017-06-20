package it.hackcaffebabe.jdrive.fs.watcher.events;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * Class that contains information about events detected from File System by
 * Watcher. WatcherEvent's subclasses map the existing WatchEvent.Kind constants
 * into custom data objects.
 */
public abstract class WatcherEvent
{
    private Path file = null;
    private long timestamp = -1L;
    private String message = null;

    WatcherEvent( Path file, String message) {
        this.setPath( file );
        this.setMessage( message );
        this.setNowAsTimestamp();
    }

    /**
     * BuildNewFrom map a WatchEvent.Kind to WatcherEvent objects. Pass kind=null
     * to retrieve an Error subclass.
     * @param kind {@link WatchEvent.Kind} the kind of event generated by Watcher
     * @param file {@link java.nio.file.Path} the path to set for the event.
     * @return a subclass of {@link WatcherEvent}
     */
    public static WatcherEvent BuildNewFrom( WatchEvent.Kind kind, Path file ){
        if( kind == StandardWatchEventKinds.ENTRY_CREATE ){
            return new Create( file );
        }else if( kind == StandardWatchEventKinds.ENTRY_MODIFY ){
            return new Modify( file );
        }else if( kind == StandardWatchEventKinds.ENTRY_DELETE ){
            return new Delete( file );
        }else {
            return new Error("Error event created from builder.");
        }
    }

    /**
     * Convert a subclass of WatcherEvent into the corresponding
     * {@link WatchEvent.Kind}. Error class will return null.
     * @return {@link WatchEvent.Kind} corresponding to the subclass of
     *         WatcherEvent that Convert is called.
     */
    public abstract WatchEvent.Kind Convert();

    /**
     * @return {@link java.nio.file.Path} of the file associated at the event.
     */
    public Path getFile(){ return this.file; }

    /**
     * @return long the timestamp of associated event.
     */
    public long getTimestamp(){ return this.timestamp; }

    /**
     * @return {@link java.lang.String} a human readable message of associated
     *         event
     */
    public String getMessage(){ return this.message; }

    public void setPath( Path path ) {
        if( path != null ){
            this.file = path.toAbsolutePath();
        }
    }

    private void setNowAsTimestamp(){
        this.timestamp = System.currentTimeMillis();
    }

    private void setMessage( String msg ) {
        if( msg != null && !msg.isEmpty() ){
            this.message = msg;
        }
    }
}