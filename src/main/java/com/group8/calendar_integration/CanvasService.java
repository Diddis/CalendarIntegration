package com.group8.calendar_integration;
import com.group8.calendar_integration.external.CanvasCalendarRequest;
import com.group8.calendar_integration.model.CalendarEvent;
import com.group8.calendar_integration.model.PostResult;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;


/**
 * Class for mapping TimeEdit calendar data to Canvas json format, 
 * and POST REST service method to Canvas.
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

            StringBuilder description = new StringBuilder();         // string together several fields for description
            // Add course code and name
            if (e.getKurskod() != null && !e.getKurskod().isBlank()) {
                description.append(e.getKurskod());
                if (e.getKurs() != null && !e.getKurs().isBlank()) {
                    description.append(", ").append(e.getKurs());
                }
                description.append("<br>");
            }
            
            //  Include person if present
            if (e.getPerson() != null && !e.getPerson().isBlank()) {
                description.append(e.getPerson()).append("<br>");
            }

            // Include zoomlink if present
            if (e.getZoomLink() != null && !e.getZoomLink().isBlank()) {
                if (description.length() > 0) {
                    description.append("<br>"); 
                }
                description.append("Zoom-länk: ").append(e.getZoomLink());
            }
            
            String comment = "";
            if (e.getComments() != null && !e.getComments().isBlank()) {
                comment = e.getComments();
                // AI-tips: Om du vill att kommentaren ska synas i Canvas-beskrivningen:
                //if (description.length() > 0) description.append("<br>");   // kommenterade bort så att den inte syns på två ställen i canvas kalendern
                //description.append("Kommentar: ").append(comment);
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

        }

        return requests;   // return array list of CanvasCalendarRequest objects

    }

    /* REST call to Canvas API to post calendar events
     * The URL structure is based on Canvas API documentation at https://canvas.instructure.com/doc/api/calendar_events.html#method.calendar_events.create
     * Loop through reservations and post each reservation/event using REST
     * 
     * Returns PostResult object with number of successful and failed posts, along with list of failed posts if any
     */

    public PostResult RESTPostCanvasEvents(List<CanvasCalendarRequest> canvasEvents) {

        // Construct the URL for posting calendar events
        String canvasURL = "https://canvas.ltu.se/api/v1/calendar_events";

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(canvasToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Error handling if a post fails. Collect events that fail
        List<String> failedEvents = new ArrayList<>();
        // Success counter
        int successCount = 0;

        // Post each event to Canvas
        for (CanvasCalendarRequest e : canvasEvents) {

            try {
                // Wrap body and headers into httpentity. 
                HttpEntity<CanvasCalendarRequest> requestEntity = new HttpEntity<>(e, headers);

                // Post the event
                ResponseEntity<String> response = restTemplate.postForEntity(canvasURL, requestEntity, String.class);

                // Check if status is 200-299
                if (response.getStatusCode().is2xxSuccessful()) {
                    successCount++;
                    System.out.println("Skickar event med context_code: " + e.getContextCode());   // testing
                } else {
                    // Log the failed event
                    failedEvents.add(e.getTitle() + " at " + e.getStartAt());
                }

            // Catch other RestClientExceptions (like network, timeout, other errors)
            } catch (RestClientException ex) {
                failedEvents.add(e.getTitle() + " at " + e.getStartAt());
            }
        } // end for loop

        // Return result message
        return new PostResult(
            successCount,
            failedEvents.size(),
            failedEvents);

    } // end method

}