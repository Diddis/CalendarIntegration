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

        // Make an instanace of RestTemplate - Spring’s HTTP client for making HTTP requests.
        // In this case, using to get content from the static JSON URL
        RestTemplate restTemplate = new RestTemplate();

        // Send HTTP GET request to the url and convert the content into String.
        // (Converting first to String to bypass the fact that the server sends json data 
        // but marks it as text/html, so RestTemplate isn't reading it as json data
        // so can't automatically turn it into java object - manually doing that with mapper below.
        String jsonString = restTemplate.getForObject(jsonUrl, String.class);

        // Create object mapper instance
        ObjectMapper mapper = new ObjectMapper();
            
        // Convert JSON string to object (child objects - TimeEditReservation objects - are 
        // automatically created when root is called)
        TimeEditRoot calendar = mapper.readValue(jsonString, TimeEditRoot.class);

        System.out.println("Calendar Object\n" + calendar);    // for testing

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
