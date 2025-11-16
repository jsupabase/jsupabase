package io.github.jsupabase.realtime.enums;

/**
 * Defines the types of events a Channel can listen to.
 *
 * @author neilhdezs
 * @version 0.1.0
 */
public enum RealtimeEvent {
    /** - All events - **/
    ALL("*"),

    /** - Database Inserts - **/
    INSERT("INSERT"),

    /** - Database Updates - **/
    UPDATE("UPDATE"),

    /** - Database Deletes - **/
    DELETE("DELETE"),

    /** - Presence state sync - **/
    SYNC("sync"),

    /** - Presence user join - **/
    JOIN("join"),

    /** - Presence user leave - **/
    LEAVE("leave");

    private final String phxEvent;

    RealtimeEvent(String phxEvent) {
        this.phxEvent = phxEvent;
    }

    public String getPhxEvent() {
        return phxEvent;
    }
}