package com.group8.calendar_integration;

import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;

/**
 * Class for converting TimeEdit json data to usable Canvas
 * calendar data, and POST REST service to Canvas (although we might be able to
 * have all this in one combined class. TBD)
 * 
 * 
 *  timeedit                          canvas
 *  ---------------------------------------------------------------
 *  startdate+starttime               calendar_event[start_at]
 *  enddate+endtime                   calendar_event[end_at]
 *  columns: aktivitet                calendar_event[title]
 *  columns: plats, lokal             calendar_event[location_name]
 *  columns: anställd, student        calendar_event[description]
 *  columns: möteslänk                calendar_event[description]
 *  columns: grupp/grupper            calendar_event[description]
 *  columns: kommentar                calendar_event[description]
 * 
 */
@Service
public class CanvasService {

    //private final RestTemplate restTemplate = new RestTemplate();

    // Assuming your authentication token is saved in your computer's system environment
    //private final String canvasToken = System.getenv("CANVAS_API_TOKEN");


    /* Method for mapping TimeEdit Json data into format for Canvas, using json tree mapper
     */


    /* Loop through reservations and post each reservation/event using REST
     */

}
