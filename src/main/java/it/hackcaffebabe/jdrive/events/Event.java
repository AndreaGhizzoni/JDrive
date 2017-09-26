package it.hackcaffebabe.jdrive.events;

import it.hackcaffebabe.jdrive.util.DateUtils;

/**
 * TODO add doc
 */
public abstract class Event
{
    private long timestamp = -1;
    private String message = null;

    public Event( String message ){
        this.setMessage( message );
        this.setNowAsTimestamp();
    }

    public Event( long timestamp, String message ){
        this.setMessage( message );
        this.setTimestamp( timestamp );
    }

    private void setMessage( String msg ) {
        if( msg != null && !msg.isEmpty() ){
            this.message = msg;
        }
    }

    private void setNowAsTimestamp(){
        setTimestamp( System.currentTimeMillis() );
    }

    private void setTimestamp( long timestamp ){
        this.timestamp = timestamp;
    }

    /** @return long the timestamp of associated event. */
    public long getTimestamp(){ return this.timestamp; }

    /** @return {@link java.lang.String} a human readable message of associated
     *          event */
    public String getMessage(){ return this.message; }

    @Override
    public String toString(){
        String l = this.getTimestamp() == 0L ?
                "0" : DateUtils.formatTimestamp( this.getTimestamp() );
        String m = this.getMessage() == null ? "null" : this.getMessage();

        StringBuilder b = new StringBuilder();
        b.append("{");
        b.append(" \"kind\": ").append("\"").append( "%s" ).append("\",");
        b.append(" \"timestamp\": ").append("\"").append( l ).append("\",");
        b.append(" \"message\": ").append("\"").append( m ).append("\"");
        b.append(" \"details\": ").append("{ ").append( "%s" ).append(" }");
        b.append("}");
        return b.toString();
    }
}
