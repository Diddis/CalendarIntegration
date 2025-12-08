package com.group8.calendar_integration.Model;

import java.util.List;

/**
 * Model class for child-level TimeEdit JSON fields.
 * Immutable to avoid editing calendar data during transfer.
 * 
 * Using this tutorial for json-pojo conversion:
 * https://www.digitalocean.com/community/tutorials/jackson-json-java-parser-api-example-tutorial
 * https://blogs.oracle.com/javamagazine/java-json-serialization-jackson/
 */
public final class TimeEditReservation {

    private final String id;
    private final String startdate;
    private final String starttime;
    private final String enddate;
    private final String endtime;
    private final List<String> columns;

    public TimeEditReservation(String id, String startdate, String starttime, 
        String enddate, String endtime, List<String> columns) {

        this.id = id;
        this.startdate = startdate;
        this.starttime = starttime;
        this.enddate = enddate;
        this.endtime = endtime;
        this.columns = columns;
    }

    public String getId() {
        return id;
    }

    public String getStartdate() {
        return startdate;
    }

    public String getStarttime() {
        return starttime;
    }

    public String getEnddate() {
        return enddate;
    }

    public String getEndtime() {
        return endtime;
    }

    public List<String> getColumns() {
        return columns;
    }

    @Override
    public String toString() {
        return "TimeEditReservation [id=" + id + ", startdate=" + startdate + ", starttime=" + starttime + ", enddate="
                + enddate + ", endtime=" + endtime + ", columns=" + columns + "]";
    }




    

}
