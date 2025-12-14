package com.group8.calendar_integration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.group8.calendar_integration.Model.CalendarEvent;
import com.group8.calendar_integration.Model.CanvasCalendarRequest;
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



    /* Method for mapping TimeEdit objects to calender event objects for API use, 
     * including for showing info in frontend.
     * Returns list of CalendarEvents objects.
    */
   public static List<CalendarEvent> mapToCalendarEvents(List<TimeEditReservation> reservations) {
        
        List<CalendarEvent> events = new ArrayList<>();

        // For each TimeEdit reservation, map to CalendarEvents format
        for (TimeEditReservation r: reservations) {

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

        return events;   // return array list of CalendarEvents objects
    }


    /* Method for mapping CalendarEvent objects to CanvasCalendarRequest objects for 
     * POSTING to Canvas API.
     * 
     * Returns list of CanvasCalendarRequest objects
    */
    public static List<CanvasCalendarRequest> mapToCanvasCalendar(List<CalendarEvent> events, 
        String contextCode) {
        
        List<CanvasCalendarRequest> requests = new ArrayList<>();

        // For each TimeEdit reservation, map to Canvas event format
        // Need to hange slightly so that fields in "description" that have no value are not displayed 
        for (CalendarEvent e: events) {

            StringBuilder description = new StringBuilder();
            // Include person if present
            if (e.getPerson() != null && !e.getPerson().isEmpty()) {
                description.append(e.getPerson());
            }
            // Include zoomlink if present
            if (e.getZoomLink() != null && !e.getZoomLink().isEmpty()) {
                if (description.length() > 0) description.append("\n"); {
                    description.append("Zoom-länk: ").append(e.getZoomLink());
            }

            String comments = "";
            if (e.getcomments() != null && !e.getcomments().isEmpty()) {
                comments = e.getcomments();
            }

            CanvasCalendarRequest request = new CanvasCalendarRequest(
                contextCode, 
                e.getActivity(),       // title
                e.getStartAt().toString(),
                e.getEndAt().toString(),
                e.getLocationName(),
                description.toString(),
                comments);

            requests.add(request);

            }
        }

        return requests;   // return array list of CanvasCalendarRequest objects

    }

    /* REST call to Canvas API to post calendar events
     * Loop through reservations and post each reservation/event using REST
     */

    public String RESTPostCanvasEvents(List<CanvasCalendarRequest> canvasEvents, String contextCode) {
        try {
            for (CanvasCalendarRequest e : canvasEvents) {
                // Post each event to Canvas
                // The URL structure is based on Canvas API documentation at https://canvas.instructure.com/doc/api/calendar_events.html#method.calendar_events.create
                
                // Construct the URL for posting calendar events
                String canvasURL = "https://canvas.instructure.com/api/v1/courses/" + contextCode + "/calendar_events?access_token=" + canvasToken;

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
