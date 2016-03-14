package it.hackcaffebabe.jdrive.fs;

import it.hackcaffebabe.jdrive.util.DateUtils;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * Object created by Watcher class when a detect a change of File System.
 */
public class DetectedEvent {
    // -1 : some error, explanation in message
    //  0 : not set
    //  1 : CREATE
    //  2 : MODIFY
    //  3 : DELETE
    private int kindOfEvent = 0;
    // null : not set
    private String filePath = null;
    // -1 : not set
    private long lastModify = -1L;
    // null : not set
    private String message = null;

    /** Create an empty DetectedObject */
    public DetectedEvent(){}

    /**
     * Create a DetectedEvent by setting all the fields.
     * @param kind {@link java.nio.file.WatchEvent.Kind} the Kind of event.
     * @param file {@link java.nio.file.Path} the path of changing object.
     * @param message {@link java.lang.String} explanation message of event.
     */
    public DetectedEvent( WatchEvent.Kind kind, Path file, String message ) {
        this.setKind(kind);
        this.setFile(file);
        this.setMessage(message);
    }

    /**
     * Create a DetectedEvent by setting the minimum parameters. The message
     * argument is auto-generated based on kind and file argument
     * @param kind {@link java.nio.file.WatchEvent.Kind} the Kind of event.
     * @param file {@link java.nio.file.Path} the path of changing object.
     */
    public DetectedEvent( WatchEvent.Kind kind, Path file ){
        this.setKind(kind);
        this.setFile(file);
    }

//==============================================================================
//  METHODS
//==============================================================================
    /** @return true if this is an error event, false otherwise.
     *          If returns true check getMessage() for explanation. */
    public boolean containError(){ return this.kindOfEvent == -1; }

    /** @return true if this event is a Creation Event, false otherwise. */
    public boolean isEventCreate(){ return this.kindOfEvent == 1; }

    /** @return true if this event is a Modification Event, false otherwise. */
    public boolean isEventModify(){ return this.kindOfEvent == 2; }

    /** @return true if this event is a Delete Event, false otherwise. */
    public boolean isEventDelete(){ return this.kindOfEvent == 3; }

    /** @return true if this event is empty, meaning that is made by empty
     *          constructor. */
    public boolean isEmpty(){ return this.kindOfEvent == 0; }

//==============================================================================
//  SETTER
//==============================================================================
    /* Set the Kind of detection. */
    private void setKind( WatchEvent.Kind kind ) {
        if( kind == null ) {
            this.kindOfEvent = -1;
            this.setMessage("null passed to setKind() method.");
        }else if( kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            this.kindOfEvent = 1;
            this.setMessage("Creation event detected.");
        }else if( kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
            this.kindOfEvent = 2;
            this.setMessage("Modification event detected.");
        }else if( kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            this.kindOfEvent = 3;
            this.setMessage("Delete event detected.");
        }else {
            this.kindOfEvent = 0; // no set for all the unmatched case
        }
    }

    /* Set the Detected Object from Watcher in Path format.
     * If argument passing is null, nothing will set. */
    private void setFile(Path path ) {
        if( path != null ){
            File tmp = path.toFile();
            this.filePath = tmp.getAbsolutePath();
            this.setLastModify(tmp.lastModified());
        }
    }

    /* Set the last modify of Detected Event by Watcher.
     * If argument passing is <= 0, nothing will set. */
    private void setLastModify( long lastModify ) {
        if( lastModify > 0L )
            this.lastModify = lastModify;
    }

    /* TODO add comment */
    private void setMessage( String msg ) {
        if( msg != null && !msg.isEmpty() ){
            // TODO maybe escaping "\n" and stuff like that
            this.message = msg;
        }
    }

//==============================================================================
//  GETTER
//==============================================================================
    /**
     * Return the kind of change detected. Returns null if kind of event set
     * is not ENTRY_CREATE, ENTRY_MODIFY or ENTRY_DELETE.
     * @return {@link java.nio.file.WatchEvent.Kind} the kind of change.
     */
    public WatchEvent.Kind getKind() {
        switch (this.kindOfEvent){
            case -1: return null;  // error
            case  0: return null;  // not set
            case  1: return StandardWatchEventKinds.ENTRY_CREATE;
            case  2: return StandardWatchEventKinds.ENTRY_MODIFY;
            case  3: return StandardWatchEventKinds.ENTRY_DELETE;
            default: return null;
        }
    }

    /**
     * @return {@link java.lang.String} that represents the absolute path
     * of detected file event.
     */
    public String getFile(){ return this.filePath; }

    /** @return {@link java.lang.Long} the last modify of detected object. */
    public long getLastModify(){ return this.lastModify; }

    /** @return TODO add doc */
    public String getMessage(){ return this.message; }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public String toString(){
        String k = this.getKind() == null ? "null" : this.getKind().toString();
        String p = this.getFile() == null ? "null" : this.getFile();
        String l = this.getLastModify() == 0L ? "0" :
                DateUtils.fromLongToString(getLastModify(), null);
        String m = this.getMessage() == null ? "null" : this.getMessage();

        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append(" \"kind\": ")
                .append("\"").append( k ).append("\"").append(",");
        b.append(" \"path\": ")
                .append("\"").append( p ).append("\"").append(",");
        b.append(" \"lastModify\": ")
                .append("\"").append( l ).append("\"").append(",");
        b.append(" \"message\": ")
                .append("\"").append( m ).append("\"");
        b.append(" }");
        return b.toString();
    }
}
