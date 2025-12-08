package com.group8.calendar_integration;
import com.group8.calendar_integration.Model.TimeEditRoot;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Class for converting TimeEdit URL to .html ending, and
 * deserialize the json data to prep for sending to Canvas
 * (although currently sends back raw json string for testing)
 */
@Service
public class TimeEditService {

    public static String convertHTMLtoJSON(String timeEditURL) {

        if (timeEditURL == null || !timeEditURL.endsWith(".html")) {
            throw new IllegalArgumentException("Invalid URL");                 // fixa sen så att mdeddelandet visas på webbsidan och är användarvänligt
        }

        return timeEditURL.substring(0, timeEditURL.length() - 5) + ".json";

    }


    /* Fetches the Json data from TimeEdit
     * We could deserialize using POJO.
     */
    public static TimeEditRoot fetchCalendar(String url) {
        
        // Convert url ending to json
        String jsonUrl = convertHTMLtoJSON(url);

        RestTemplate restTemplate = new RestTemplate();
        String jsonString = restTemplate.getForObject(jsonUrl, String.class);

        // Create object mapper instance
        ObjectMapper mapper = new ObjectMapper();
            
        // Convert JSON string to object (child objects - TimeEditReservation objects - are 
        // automatically created when root is called)
        TimeEditRoot calendar = mapper.readValue(jsonString, TimeEditRoot.class);
        System.out.println("Calendar Object\n" + calendar);

        return calendar;
        

        // This error handling doesn't work, don't know why.
        /*catch (JacksonIOException  e) {
            throw new TimeEditParseException("Error while parsing TimeEdit Json data.", e);
        }*/
    }

    // Error handling
    public class TimeEditParseException extends RuntimeException {
        public TimeEditParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }


}
