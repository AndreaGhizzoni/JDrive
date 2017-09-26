package it.hackcaffebabe.jdrive.remote.watcher.events;

import com.google.api.services.drive.model.File;
import it.hackcaffebabe.jdrive.events.Event;

import java.nio.file.Path;

/**
 * TODO add doc
 */
public class Download extends Event
{
    private File remoteFile;
    private Path localPath;

    public Download( File remoteFile, Path localPath, String message ) {
        super( message );
        this.setLocalPath( localPath );
        this.setRemoteFile( remoteFile );
    }

    private void setRemoteFile( File remoteFile ){
        this.remoteFile = remoteFile;
    }

    private void setLocalPath( Path localPath ){
        this.localPath = localPath;
    }

    @Override
    public String toString(){
        String superString = super.toString();
        StringBuilder b = new StringBuilder();
        b.append(" \"remoteFile\": ")
                .append("\"").append( remoteFile.toString() ).append("\"");
        b.append(" \"localPath\": ")
                .append("\"").append( localPath.toString() ).append("\"");

        return String.format( superString, "download", b.toString() );
    }
}
