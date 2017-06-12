package it.hackcaffebabe.jdrive.fs.watcher.events;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * Class that contains information about events detected from File System by
 * Watcher.
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

    public Path getFile(){ return this.file; }

    public long getTimestamp(){ return this.timestamp; }

    public String getMessage(){ return this.message; }

//    public boolean containError(){ return this.kindOfEvent == -1; }
//    public boolean isEventCreate(){ return this.kindOfEvent == 1; }
//    public boolean isEventModify(){ return this.kindOfEvent == 2; }
//    public boolean isEventDelete(){ return this.kindOfEvent == 3; }
//    public boolean isEmpty(){ return this.kindOfEvent == 0; }

    /* Set the Detected Object from Watcher in Path format.
     * If argument passing is null, nothing will set. */
    public void setPath(Path path ) {
        if( path != null ){
            this.file = path.toAbsolutePath();
        }
    }

    private void setNowAsTimestamp(){
        this.timestamp = System.currentTimeMillis();
    }

    /* Set a human readable message to explain the kind of event that occurs.
    *  Extremely useful in Error event. */
    private void setMessage( String msg ) {
        if( msg != null && !msg.isEmpty() ){
            this.message = msg;
        }
    }
}
