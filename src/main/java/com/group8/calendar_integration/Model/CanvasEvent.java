package com.group8.calendar_integration.Model;

/**
 * Model class for Canvas JSON fields.
 * Immutable to avoid editing calendar data during transfer.
 * 
 * Using this tutorial for json-pojo conversion:
 * https://www.digitalocean.com/community/tutorials/jackson-json-java-parser-api-example-tutorial
 * https://blogs.oracle.com/javamagazine/java-json-serialization-jackson/
 */
public final class CanvasEvent {

    private final String title;
    private final String start_at;
    private final String end_at;
    private final String location_name;
    private final String description;
    private final String context_code;

    public CanvasEvent(String title, String start_at, String end_at, String location_name, 
        String description, String context_code) {

        this.title = title;
        this.start_at = start_at;
        this.end_at = end_at;
        this.location_name = location_name;
        this.description = description;
        this.context_code = context_code;
    }

    public String getTitle() {
        return title;
    }

    public String getStart_at() {
        return start_at;
    }

    public String getEnd_at() {
        return end_at;
    }

    public String getLocation_name() {
        return location_name;
    }

    public String getDescription() {
        return description;
    }

    public String getContext_code() {
        return context_code;
    }

    @Override
    public String toString() {
        return "CanvasEvent [title=" + title + ", start_at=" + start_at + ", end_at=" + end_at + ", location_name="
                + location_name + ", description=" + description + ", context_code=" + context_code + "]";
    }

    

    

}
