package it.hackcaffebabe.jdrive.remote.watcher.events;

import it.hackcaffebabe.jdrive.events.Event;

/**
 * TODO add doc
 */
public class Error extends Event
{
    public Error( String message ){
        super( message );
    }

    @Override
    public String toString(){
        String superString = super.toString();
        return String.format( superString, "error", "" );
    }
}
