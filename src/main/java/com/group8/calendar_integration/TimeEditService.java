package com.group8.calendar_integration;

import org.springframework.web.client.RestTemplate;
//import tools.jackson.databind.JsonNode;
//import tools.jackson.databind.ObjectMapper;

/**
 * Class for converting TimeEdit URL to .html ending, and
 * deserialize the json data to prep for sending to Canvas
 * (although currently sends back raw json string for testing)
 */
public class TimeEditService {

    public static String convertHTMLtoJSON(String timeEditURL) {

        if (timeEditURL == null || !timeEditURL.endsWith(".html")) {
            throw new IllegalArgumentException("Invalid URL");                 // fixa sen så att mdeddelandet visas på webbsidan och är användarvänligt
        }

        return timeEditURL.substring(0, timeEditURL.length() - 5) + ".json";

    }


    /* Fetches the Json data from TimeEdit
     * We could deserialize into a json tree structure for "easy"
     * selection of the data we need for Canvas. But for now, printing
     * the json String for test/early dev.
     */
    public static String fetchJson(String url) {
        
        String jsonUrl = convertHTMLtoJSON(url);

        RestTemplate restTemplate = new RestTemplate();
        String jsonString = restTemplate.getForObject(jsonUrl, String.class);

        //ObjectMapper mapper = new ObjectMapper();
        //return mapper.readTree(jsonString);

        return jsonString;
    }


}
