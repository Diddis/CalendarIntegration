package com.group8.calendar_integration.Model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model class for top-level TimeEdit JSON fields.
 * Root object for TimeEdit data.
 * Immutable to avoid editing calendar data during transfer.
 * 
 * Using this tutorial for json-pojo conversion:
 * https://www.digitalocean.com/community/tutorials/jackson-json-java-parser-api-example-tutorial
 * https://blogs.oracle.com/javamagazine/java-json-serialization-jackson/
 */
@JsonIgnoreProperties(ignoreUnknown = true)   // ignore info field we do not need
public class TimeEditRoot {

    private List<String> columnheaders;
    private List<TimeEditReservation> reservations;

    public TimeEditRoot() {
    }

    public TimeEditRoot(List<String> columnheaders, List<TimeEditReservation> reservations) {
        this.columnheaders = columnheaders;
        this.reservations = reservations;
    }

    public List<String> getColumnheaders() {
        return columnheaders;
    }

    public List<TimeEditReservation> getReservations() {
        return reservations;
    }

    @Override
    public String toString() {
        return "TimeEditRoot [columnheaders=" + columnheaders + ", reservations=" + reservations + "]";
    }

    

    

}
