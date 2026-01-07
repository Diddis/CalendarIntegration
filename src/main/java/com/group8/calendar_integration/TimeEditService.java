package com.group8.calendar_integration;
import com.group8.calendar_integration.external.TimeEditReservation;
import com.group8.calendar_integration.external.TimeEditRoot;
import com.group8.calendar_integration.model.CalendarEvent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


/**
 * Class for converting TimeEdit URL to .html ending, and
 * deserialize the json data to prep for sending to Canvas.
 */
@Service
public class TimeEditService {

    /* Fetches the Json data from TimeEdit. Deserialize using POJO.
     * Returns TimeEditRoot object containing list of TimeEditReservation objects.
     */
    public static TimeEditRoot fetchCalendar(String url) {
        // Validate URL
        URI uri = parseAndValidate(url);

        // Convert url ending to json
        URI jsonUri = convertHTMLtoJSON(uri);

        // Make an instanace of RestTemplate - Spring’s HTTP client for making HTTP requests.
        // In this case, using to get content from the static JSON URL
        RestTemplate restTemplate = new RestTemplate();

        // Send HTTP GET request to the url and convert the content into String.
        // (Converting first to String to bypass the fact that the server sends json data 
        // but marks it as text/html, so RestTemplate isn't reading it as json data
        // so can't automatically turn it into java object - manually doing that with mapper below.
        String jsonString;
        try {
            jsonString = restTemplate.getForObject(jsonUri, String.class);
        } catch (RestClientException e) {    // handle errors if failed to fetch data
            throw e;
        }
        
        // Deserialize JSON string to TimeEditRoot object
        try {
            // Create object mapper instance
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());     // to handle Java 8 date/time types
            
            // Convert JSON string to object (child objects - TimeEditReservation objects - are 
            // automatically created when root is called)
            TimeEditRoot calendar = mapper.readValue(jsonString, TimeEditRoot.class);

            System.out.println("Calendar Object\n" + calendar);    // for testing

            return calendar;

        } catch (IOException e) {
            throw new TimeEditParseException(
                "Error while parsing TimeEdit Json data.", e);
        }

    }


    /* Method for mapping TimeEdit objects to calender event objects for internal API use, 
     * including for showing info in frontend.
     * Returns list of CalendarEvents objects.
    */
    public List<CalendarEvent> mapToCalendarEvents(List<TimeEditReservation> reservations) {
        
        List<CalendarEvent> events = new ArrayList<>(); 
        Set<String> courseCodes = new java.util.HashSet<>();

        // For each TimeEdit reservation, map to CalendarEvents format
        for (TimeEditReservation r: reservations) {

            // Only return events with a course code, so that we don't post generic events (like holidays)
            //if (r.getColumns().size() < 2) continue;  // safety check to avoid index out of bounds  
            String kurskod = r.getColumns().get(2).trim();
            if (kurskod.isEmpty()) continue;
            courseCodes.add(kurskod);  // collect course codes for validation

            LocalDate startDate = LocalDate.parse(r.getStartdate());
            LocalTime startTime = LocalTime.parse(r.getStarttime());
            LocalDate endDate = LocalDate.parse(r.getEnddate());
            LocalTime endTime = LocalTime.parse(r.getEndtime());

            Instant startAt = ZonedDateTime.of(startDate, startTime,
                ZoneOffset.UTC).toInstant();
            Instant endAt = ZonedDateTime.of(endDate, endTime,
                ZoneOffset.UTC).toInstant();

            // Mapping TimeEdit json columns
            String kurs = r.getColumns().get(3);
            String activity = r.getColumns().get(0);
            String location = r.getColumns().get(1);
            String person = r.getColumns().get(5);
            String zoomLink = r.getColumns().get(8);
            String comment = r.getColumns().get(7); 

            events.add(new CalendarEvent(kurskod, kurs, startAt, endAt, activity, 
                location, person, zoomLink, comment));               

        }

        // Validate that only one course is present
        System.out.println("DEBUG: courseCodes = " + courseCodes);   // test
        validateSingleCourse(courseCodes);

        return events;   // return array list of CalendarEvents objects   
        
    }


    // --------------- Helper methods below --------------------------

    /* Helper method for converting HTML URL to JSON URL */
    private static URI convertHTMLtoJSON(URI timeEditURI) {

        String jsonPath = timeEditURI.getPath().substring(
            0, timeEditURI.getPath().length() - 5) + ".json";

        return timeEditURI.resolve(jsonPath);
    }

    /* Validate the URL, such as if it comes from a different site, is empty, etc. */
    private static URI parseAndValidate(String url) {
        
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL kan inte vara tom.");
        }
        
        try {
            URI uri = new URI(url);

            if (!uri.getHost().toLowerCase().endsWith("timeedit.net")) {
                throw new IllegalArgumentException("URL måste komma från TimeEdit.");
            }

            if (!uri.getPath().endsWith(".html")) {
                throw new IllegalArgumentException("URL måste sluta med .html.");
            }

            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("Endast URL med HTTPS protokoll.");
            }

            return uri;

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Ogiltig URL: " + e.getMessage());
        }
            
    }

    /* Helper method for validating that only events
     * for one course are present in TimeEdit data.
     */
    private void validateSingleCourse(Set<String> courseCodes) {
        if (courseCodes.size() > 1) {
            throw new IllegalArgumentException("TimeEdit kalendern innehåller händelser från flera kurser. Hämta en ny TimeEdit kalender URL för endast en kurs.");
        }
    }

    
    // Error handling
    public static class TimeEditParseException extends RuntimeException {
        public TimeEditParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    


}