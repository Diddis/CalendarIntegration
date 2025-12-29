package com.group8.calendar_integration;
import com.group8.calendar_integration.external.CanvasCalendarRequest;
import com.group8.calendar_integration.external.TimeEditReservation;
import com.group8.calendar_integration.model.CalendarEvent;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;


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
 *  columns: kommentar                comments                      (editable, optional)
 * 
 */
@Service
public class CanvasService {

    // Create instance of resttemplate
    private final RestTemplate restTemplate = new RestTemplate();

    // Canvas API authentication token instance variable
    private final String canvasToken;

    // Constructor for CanvasService class
    public CanvasService() {

        // Assuming your authentication token is saved in your computer's system environment
        this.canvasToken = System.getenv("CANVAS_API_TOKEN");

        // Check if the token is not set
        if (this.canvasToken == null || this.canvasToken.isEmpty()) {
            throw new IllegalStateException("Canvas API token is not set in environment variables.");
        }
    }


    /* Method for mapping TimeEdit objects to calender event objects for internal API use, 
     * including for showing info in frontend.
     * Returns list of CalendarEvents objects.
    */
   public List<CalendarEvent> mapToCalendarEvents(List<TimeEditReservation> reservations) {
        
        List<CalendarEvent> events = new ArrayList<>(); 

        // For each TimeEdit reservation, map to CalendarEvents format
        for (TimeEditReservation r: reservations) {

            // Only return events with a course code, so that we don't post generic events (like holidays)
            if (r.getColumns().get(2) != null && !r.getColumns().get(2).isBlank()) {

                LocalDate startDate = LocalDate.parse(r.getStartdate());
                LocalTime startTime = LocalTime.parse(r.getStarttime());
                LocalDate endDate = LocalDate.parse(r.getEnddate());
                LocalTime endTime = LocalTime.parse(r.getEndtime());

                Instant startAt = ZonedDateTime.of(startDate, startTime,
                    ZoneOffset.UTC).toInstant();
                Instant endAt = ZonedDateTime.of(endDate, endTime,
                    ZoneOffset.UTC).toInstant();

                // Mapping TimeEdit json columns
                String kurskod = r.getColumns().get(2);
                String kurs = r.getColumns().get(3);
                String activity = r.getColumns().get(0);
                String location = r.getColumns().get(1);
                String person = r.getColumns().get(5);
                String zoomLink = r.getColumns().get(8);
                String comment = r.getColumns().get(7); 

                events.add(new CalendarEvent(kurskod, kurs, startAt, endAt, activity, 
                    location, person, zoomLink, comment));               

            }
        }

        return events;   // return array list of CalendarEvents objects   
        
    }


    /* Method for mapping nested CalendarEvent objects to CanvasCalendarRequest objects for 
     * POSTING to Canvas API.
     * 
     * Returns list of CanvasCalendarRequest objects
    */
    public List<CanvasCalendarRequest> mapToCanvasCalendar(List<CalendarEvent> events, 
        String contextCode) {
        
        List<CanvasCalendarRequest> requests = new ArrayList<>();

        // For each TimeEdit reservation, map to Canvas event format
        for (CalendarEvent e: events) {

            StringBuilder description = new StringBuilder();         // string together several fields for Canvas description
            //  Include person if present
            if (e.getPerson() != null && !e.getPerson().isBlank()) {
                description.append(e.getPerson()).append("<br>");
            }

            // Include zoomlink if present
            if (e.getZoomLink() != null && !e.getZoomLink().isBlank()) {
                if (description.length() > 0) description.append("<br>"); {
                    description.append("Zoom-länk: ").append(e.getZoomLink());
		} // re-added braces since it is a nested if-statement
            }
            
            String comment = "";
            if (e.getComments() != null && !e.getComments().isBlank()) {
                comment = e.getComments();
                // AI-tips: Om du vill att kommentaren ska synas i Canvas-beskrivningen:
                if (description.length() > 0) description.append("<br>");
                description.append("Kommentar: ").append(comment);
            }
            
            CanvasCalendarRequest request = new CanvasCalendarRequest(
                contextCode, 
                e.getActivity() + " (TE)",       // add TE title to indicate TimeEdit origin
                e.getStartAt().toString(),
                e.getEndAt().toString(),
                e.getLocationName(),
                description.toString(),
                comment);

            requests.add(request);

            // KS: Removed the extra/faulty curly braces previously here 
        }

        return requests;   // return array list of CanvasCalendarRequest objects

    }

    /* REST call to Canvas API to post calendar events
     * Loop through reservations and post each reservation/event using REST
     * 
     * Returns success message or throws exception on error
     */

    public String RESTPostCanvasEvents(List<CanvasCalendarRequest> canvasEvents) {
        try {

            // Construct the URL for posting calendar events
            String canvasURL = "https://canvas.ltu.se/api/v1/calendar_events";

            // Build headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(canvasToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            for (CanvasCalendarRequest e : canvasEvents) {
                // Post each event to Canvas
                // The URL structure is based on Canvas API documentation at https://canvas.instructure.com/doc/api/calendar_events.html#method.calendar_events.create

                // Wrap body and headers into httpentity
                HttpEntity<CanvasCalendarRequest> requestEntity = new HttpEntity<>(e, headers);

                // Post the event
                restTemplate.postForObject(canvasURL, requestEntity, String.class);
            }
            return "All events posted successfully to Canvas.";
        } 
        catch (RestClientException e) {
            throw new RuntimeException("Error posting events to Canvas", e);
        }
    }


}
