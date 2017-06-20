package it.hackcaffebabe.jdrive.fs.watcher.events;

import it.hackcaffebabe.jdrive.util.DateUtils;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

/**
 * This class represents a Create event associated to a file path.
 */
public class Create extends WatcherEvent
{
    /**
     * Instance a Create event associated to a given file path.
     * @param file {@link java.nio.file.Path} the file path of Create event.
     */
    public Create( Path file ){ this( file, "Creation event detected." ); }

    /**
     * Instance a Create event associated to a given file path and custom human
     * readable message.
     * @param file {@link java.nio.file.Path} the file path of Create event.
     * @param message {@link java.lang.String} a human readable message.
     */
    public Create( Path file, String message ) { super( file, message ); }

    @Override
    public WatchEvent.Kind Convert() {
        return StandardWatchEventKinds.ENTRY_CREATE;
    }

    @Override
    public String toString(){
        String p = this.getFile() == null ? "null" : this.getFile().toString();
        String l = this.getTimestamp() == 0L ?
                "0" : DateUtils.formatTimestamp( this.getTimestamp() );
        String m = this.getMessage() == null ? "null" : this.getMessage();

        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append(" \"kind\": ").append("\"").append( "create" ).append("\",");
        b.append(" \"path\": ").append("\"").append( p ).append("\",");
        b.append(" \"timestamp\": ").append("\"").append( l ).append("\",");
        b.append(" \"message\": ").append("\"").append( m ).append("\"");
        b.append("}");
        return b.toString();
    }
}
