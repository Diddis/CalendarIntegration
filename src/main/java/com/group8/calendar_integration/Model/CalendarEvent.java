package com.group8.calendar_integration.Model;

import java.time.Instant;

/**
 * Model class for data needed in app and frontend. Then also used to populate 
 * CanvasCreateCalendarRequest objects for POST requests to Canvas.
 */
public class CalendarEvent {

    private String kurskod;
    private String kurs;
    private Instant startAt;       // Using Instant type for date-time fields
    private Instant endAt;    
    private String activity;
    private String locationName;
    private String person;
    private String zoomLink;
    private String comments;


    public CalendarEvent(String kurskod, String kurs, Instant startAt, Instant endAt, String activity,
            String locationName, String person, String zoomLink, String comments) {
        this.kurskod = kurskod;
        this.kurs = kurs;
        this.startAt = startAt;
        this.endAt = endAt;
        this.activity = activity;
        this.locationName = locationName;
        this.person = person;
        this.zoomLink = zoomLink;
        this.comments = comments;
    }


    public String getKurskod() {
        return kurskod;
    }


    public void setKurskod(String kurskod) {
        this.kurskod = kurskod;
    }


    public String getKurs() {
        return kurs;
    }


    public void setKurs(String kurs) {
        this.kurs = kurs;
    }


    public Instant getStartAt() {
        return startAt;
    }


    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }


    public Instant getEndAt() {
        return endAt;
    }


    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }


    public String getActivity() {
        return activity;
    }


    public void setActivity(String activity) {
        this.activity = activity;
    }


    public String getLocationName() {
        return locationName;
    }


    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }


    public String getPerson() {
        return person;
    }


    public void setPerson(String person) {
        this.person = person;
    }


    public String getZoomLink() {
        return zoomLink;
    }


    public void setZoomLink(String zoomLink) {
        this.zoomLink = zoomLink;
    }


    public String getcomments() {
        return comments;
    }


    public void setcomments(String comments) {
        this.comments = comments;
    }


    @Override
    public String toString() {
        return "CalendarEvent [activity=" + activity + ", endAt=" + endAt + ", kurs=" + kurs
         + ", kurskod=" + kurskod + ", locationName=" + locationName + ", person=" + person 
         + ", startAt=" + startAt + ", comments=" + comments + ", zoomLink=" + zoomLink + "]";
    }

    
}
