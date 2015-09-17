package it.hackcaffebabe.jdrive.fs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * Object created by Watcher class when a changing of File System is detected.
 */
public class DetectedObject {
    // -1 not set/error, 0 creation, 1 modify, 2 delete
    private int kindOfEvent = -1;
    private String filePath = null;
    private long lastModify = -1L;

    public DetectedObject(){}

    public DetectedObject(WatchEvent.Kind kind, String filePath, long lastModify) {
        setKind(kind);
        setDetectedObject(filePath);
        setLastModify(lastModify);
    }

//==============================================================================
//  SETTER
//==============================================================================
    public void setKind( WatchEvent.Kind kind ) {
        if( kind.equals(StandardWatchEventKinds.OVERFLOW) )
            this.kindOfEvent = -1;
        else if( kind.equals(StandardWatchEventKinds.ENTRY_CREATE))
            this.kindOfEvent = 0;
        else if( kind.equals(StandardWatchEventKinds.ENTRY_MODIFY))
            this.kindOfEvent = 1;
        else if( kind.equals(StandardWatchEventKinds.ENTRY_DELETE))
            this.kindOfEvent = 2;
    }

    public void setDetectedObject( String filePath ) {
        if( filePath == null || filePath.isEmpty() )
            this.filePath = null;
        else
            this.filePath = filePath;
    }

    public void setDetectedObject( File file ){
        if( file != null )
            this.filePath = file.getAbsolutePath();
    }

    public void setDetectedObject( Path path ){
        if( path != null )
            this.filePath = path.toFile().getAbsolutePath();
    }

    public void setLastModify(long lastModify) {
        if( lastModify >= 0L )
            this.lastModify = lastModify;
    }

//==============================================================================
//  GETTER
//==============================================================================
    public WatchEvent.Kind getKind() {
        switch (this.kindOfEvent){
            case -1: return null;
            case  0: return StandardWatchEventKinds.ENTRY_CREATE;
            case  1: return StandardWatchEventKinds.ENTRY_MODIFY;
            case  2: return StandardWatchEventKinds.ENTRY_DELETE;
            default: return null;
        }
    }

    public String getPathAsString() { return this.filePath; }

    public Path getPath(){ return Paths.get( this.getPathAsString() ); }

    public File getFile(){ return this.getPath().toFile(); }

    public long getLastModify() { return lastModify; }

//==============================================================================
//  OVERRIDE
//==============================================================================
    @Override
    public String toString(){
        return "TODO";
    }
}
