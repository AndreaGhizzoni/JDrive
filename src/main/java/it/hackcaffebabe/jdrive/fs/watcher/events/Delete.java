package it.hackcaffebabe.jdrive.fs.watcher.events;

import it.hackcaffebabe.jdrive.util.DateUtils;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * TODO add doc
 */
public class Delete extends WatcherEvent
{
    /**
     * TODO add doc
     * @param file
     */
    public Delete( Path file ) { this( file, "Delete event detected." ); }

    /**
     * TODO add doc
     * @param file
     * @param message
     */
    public Delete( Path file, String message ) { super( file, message ); }

    @Override
    public WatchEvent.Kind Convert() {
        return StandardWatchEventKinds.ENTRY_DELETE;
    }

    @Override
    public String toString(){
        String p = this.getFile() == null ? "null" : this.getFile().toString();
        String l = this.getTimestamp() == 0L ?
                "0" : DateUtils.formatTimestamp( this.getTimestamp() );
        String m = this.getMessage() == null ? "null" : this.getMessage();

        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append(" \"kind\": ").append("\"").append( "delete" ).append("\",");
        b.append(" \"path\": ").append("\"").append( p ).append("\",");
        b.append(" \"timestamp\": ").append("\"").append( l ).append("\",");
        b.append(" \"message\": ").append("\"").append( m ).append("\"");
        b.append("}");
        return b.toString();
    }
}
