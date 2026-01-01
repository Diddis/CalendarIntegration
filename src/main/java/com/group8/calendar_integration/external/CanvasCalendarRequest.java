package com.group8.calendar_integration.external;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model class for Canvas JSON fields. It is a nested two-layer structure 
 * in order to match required Canvas API data structure.
 * https://canvas.instructure.com/doc/api/calendar_events.html#method.calendar_events.create
 */
public final class CanvasCalendarRequest {

    @JsonProperty("calendar_event")
    private CalendarEventData calendarEvent;   // nest all fields inside this object

    public CanvasCalendarRequest(String contextCode,
                                 String title,
                                 String startAt,
                                 String endAt,
                                 String locationName,
                                 String description,
                                 String comments) {
        this.calendarEvent = new CalendarEventData(
                contextCode, title, startAt, endAt, locationName, description, comments);
    }

    // KS tillägg 2026-01-01 för felsökning
    public String getContextCode() {
        return this.calendarEvent.getContextCode();
    }

    public static class CalendarEventData {        // place nested fields in calendar_event object
        @JsonProperty("context_code")
        private String contextCode;
        @JsonProperty("title")
        private String title;
        @JsonProperty("start_at")
        private String startAt;
        @JsonProperty("end_at")
        private String endAt;
        @JsonProperty("location_name")
        private String locationName;
        @JsonProperty("description")
        private String description;
        @JsonProperty("comments")
        private String comments;

        // Constructor
        public CalendarEventData(String contextCode,
                                 String title,
                                 String startAt,
                                 String endAt,
                                 String locationName,
                                 String description,
                                 String comments) {
            this.contextCode = contextCode;
            this.title = title;
            this.startAt = startAt;
            this.endAt = endAt;
            this.locationName = locationName;
            this.description = description;
            this.comments = comments;
        }
    

        public String getContextCode() {
            return contextCode;
        }

        public void setContextCode(String contextCode) {
            this.contextCode = contextCode;
        }

        public String getTitle() {
            return title;
        }

        public String getStartAt() {
            return startAt;
        }

        public String getEndAt() {
            return endAt;
        }

        public String getLocationName() {
            return locationName;
        }

        public String getDescription() {
            return description;
        }

        public String getComments() {
            return comments;
        }


        @Override
        public String toString() {
            return "CanvasEvent [contextCode=" + contextCode + ", title=" + title 
                    + ", startAt=" + startAt + ", endAt=" + endAt + ", locationName="
                    + locationName + ", description=" + description + ", comments=" + comments + "]";
        }

        
    }
}
