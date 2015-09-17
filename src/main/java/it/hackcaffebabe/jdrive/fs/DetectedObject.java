package it.hackcaffebabe.jdrive.fs;

import it.hackcaffebabe.jdrive.util.DateUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * Object created by Watcher class when a detect a change of File System.
 */
public class DetectedObject {
    // -1 not set/error, 0 creation, 1 modify, 2 delete
    private int kindOfEvent = -1;
    private String filePath = null;
    private long lastModify = -1L;

    /**
     * Create an empty DetectedObject
     */
    public DetectedObject(){}

    /**
     * Create a DetectedObject.
     * @param kind {@link java.nio.file.WatchEvent.Kind} the Kind of event.
     * @param filePath {@link java.lang.String} the path of changing object.
     * @param lastModify {@link java.lang.Long} the timestamp of last modify.
     */
    public DetectedObject(WatchEvent.Kind kind, String filePath, long lastModify) {
        setKind(kind);
        setDetectedObject(filePath);
        setLastModify(lastModify);
    }

//==============================================================================
//  SETTER
//==============================================================================
    /**
     * Set the Kind of detection.
     * @param kind {@link java.nio.file.WatchEvent.Kind} the kind of detection.
     */
    public void setKind( WatchEvent.Kind kind ) {
        if( kind == null )
            this.kindOfEvent = -1;
        else if( kind.equals(StandardWatchEventKinds.OVERFLOW) )
            this.kindOfEvent = -1;
        else if( kind.equals(StandardWatchEventKinds.ENTRY_CREATE))
            this.kindOfEvent = 0;
        else if( kind.equals(StandardWatchEventKinds.ENTRY_MODIFY))
            this.kindOfEvent = 1;
        else if( kind.equals(StandardWatchEventKinds.ENTRY_DELETE))
            this.kindOfEvent = 2;
    }

    /**
     * Set the Detected Object from Watcher in String format.
     * If pass null or empty string, noting will set.
     * @param filePath {@link java.lang.String} in string format.
     */
    public void setDetectedObject( String filePath ) {
        if( filePath != null && !filePath.isEmpty() )
            this.filePath = filePath;
    }

    /**
     * Set the Detected Object from Watcher in File format.
     * If file passing is null, nothing will set.
     * @param file {@link java.io.File} in File format.
     */
    public void setDetectedObject( File file ){
        if( file != null )
            this.filePath = file.getAbsolutePath();
    }

    /**
     * Set the Detected Object from Watcher in Path format.
     * If path passing is null, nothing will set.
     * @param path {@link java.nio.file.Path} in Path format.
     */
    public void setDetectedObject( Path path ){
        if( path != null )
            this.filePath = path.toFile().getAbsolutePath();
    }

    /**
     * Set the last modify of Detected Object by Watcher.
     * If lastModify passing is <= 0, nothing will set.
     * @param lastModify {@link java.lang.Long} in long format.
     */
    public void setLastModify(long lastModify) {
        if( lastModify > 0L )
            this.lastModify = lastModify;
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
            case -1: return null;
            case  0: return StandardWatchEventKinds.ENTRY_CREATE;
            case  1: return StandardWatchEventKinds.ENTRY_MODIFY;
            case  2: return StandardWatchEventKinds.ENTRY_DELETE;
            default: return null;
        }
    }

    /**
     * @return {@link java.lang.String} representation of detected object
     * by watcher.
     */
    public String getPathAsString(){ return this.filePath; }

    /**
     * @return {@link java.nio.file.Path} representation of detected object by
     * watcher.
     */
    public Path getPath(){
        String p = this.getPathAsString();
        if( p == null )
            return  null;
        else
            return Paths.get( p );
    }

    /**
     * @return {@link java.io.File} representation of detected object by watcher.
     */
    public File getFile(){ return this.getPath().toFile(); }

    /**
     * @return {@link java.lang.Long} the last modify of detected object.
     */
    public long getLastModify(){ return lastModify; }

    /**
     * @return {@link java.lang.String} representation of last modify.
     */
    public String getLastModifyAsString(){
        return DateUtils.fromLongToString( this.getLastModify(), null );
    }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public String toString(){
        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append(" \"kind\": ").append("\"").append(getKind().toString()).append("\"").append(",");
        b.append(" \"path\": ").append("\"").append(getPathAsString()).append("\"").append(",");
        b.append(" \"lastModify\": ").append("\"").append(getLastModify()).append("\"");
        b.append(" }");
        return b.toString();
    }
}
