package com.group8.calendar_integration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.group8.calendar_integration.Model.CanvasEvent;
import com.group8.calendar_integration.Model.TimeEditReservation;

/**
 * Class for converting TimeEdit json data to usable Canvas
 * calendar data, and POST REST service to Canvas.
 * 
 * Using this tutorial for json-pojo conversion:
 * https://www.digitalocean.com/community/tutorials/jackson-json-java-parser-api-example-tutorial
 * https://blogs.oracle.com/javamagazine/java-json-serialization-jackson/ 
 * 
 * mapping  timeedit data to canvas data:
 * 
 *  timeedit                          canvas                        notes
 *  --------------------------------------------------------------------------
 *  startdate+starttime               calendar_event[start_at]
 *  enddate+endtime                   calendar_event[end_at]
 *  columns: aktivitet                calendar_event[title]         (editable)
 *  columns: plats, lokal             calendar_event[location_name]
 *  columns: anställd, student        calendar_event[description]   (editable, optional)
 *  columns: möteslänk                calendar_event[description]   (editable, optional)
 *  columns: grupp/grupper            calendar_event[description]   (editable, optional)
 *  columns: kommentar                comments                      (editable, optional)
 * 
 */
@Service
public class CanvasService {

    private final RestTemplate restTemplate = new RestTemplate();

    // Set date-time format
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // Assuming your authentication token is saved in your computer's system environment
    private final String canvasToken = System.getenv("CANVAS_API_TOKEN");


    /* Method for mapping TimeEdit Json data into format that can be posted to Canvas
     * CanvasEvent model parameters: String title, String start_at, String end_at,  
     * String location_name, String description, String context_code 
     * 
     * Returns list of CanvasEvent objects
    */
    public static List<CanvasEvent> mapToCanvasEvents(List<TimeEditReservation> reservations) {
        
        List<CanvasEvent> events = new ArrayList<>();

        // For each TimeEdit reservation, map to Canvas event format
        // Change slightly so that fields in "description" that have no value are not displayed 
        for (TimeEditReservation r: reservations) {

            String startAt = r.getStartdate() + "T" + r.getStarttime() + ":00";
            String endAt = r.getEnddate() + "T" + r.getEndtime() + ":00";

            // Mapping TimeEdit json columns
            String title = r.getColumns().get(0);
            String location = r.getColumns().get(1);
            String description = "Teacher/Student: " + r.getColumns().get(5) +
                            ", Group: " + r.getColumns().get(4);
            String comments = r.getColumns().get(7); 

            events.add(new CanvasEvent(title, startAt, endAt, location, description, comments));
        }
        return events;
    }

    // *** OBS! REST-metoden är inte klar: behöver spara courseId någonstans i applikationen från TimeEdit data.
    /* REST call to Canvas API to post calendar events
     * Loop through reservations and post each reservation/event using REST
     */

    public String RESTPostCanvasEvents(List<CanvasEvent> canvasEvents, String courseID) {
        try {
            for (CanvasEvent e : canvasEvents) {
                // Post each event to Canvas
                // The URL structure is based on Canvas API documentation at https://canvas.instructure.com/doc/api/calendar_events.html#method.calendar_events.create
                
                // Construct the URL for posting calendar events
                String canvasURL = "https://canvas.instructure.com/api/v1/courses/" + courseID + "/calendar_events?access_token=" + canvasToken;

                // Post the event
                restTemplate.postForObject(canvasURL, e, String.class);
            }
            return "All events posted successfully to Canvas.";
        } 
        catch (RestClientException e) {
            throw new RuntimeException("Error posting events to Canvas", e);
        }
    }


}
