package com.group8.calendar_integration;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group8.calendar_integration.external.CanvasCalendarRequest;
import com.group8.calendar_integration.external.TimeEditRoot;
import com.group8.calendar_integration.model.CalendarEvent;
import com.group8.calendar_integration.model.PostResult;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller class.
 */
@RestController
@RequestMapping("/kalender")       // Http beginning for all endpoints in this controller
public class CalendarIntegrationController {

    private final CanvasService canvasService;     // canvas service instance
    private final TimeEditService timeEditService; // timeedit service instance 

    // Constructor injection of CanvasService (for Spring to manage dependencies)
    public CalendarIntegrationController(CanvasService canvasService, TimeEditService timeEditService) {
        this.canvasService = canvasService;
        this.timeEditService = timeEditService;
    }


    /* Fetch TimeEdit calendar data.
     * Returns either mapped Canvas events or error message.
     */
    @GetMapping("/timeEditURL")    // Http endpoint for first button
    public ResponseEntity<?> getTimeEditEvents(@RequestParam String url) {

        try {
            // Fetch TimeEdit data as POJO
            TimeEditRoot calendar = TimeEditService.fetchCalendar(url);

            // Map to Canvas events
            List<CalendarEvent> calendarEvents = timeEditService.mapToCalendarEvents(calendar.getReservations());

            // Successful return Status 200
            return ResponseEntity.ok(calendarEvents);

        } catch (IllegalArgumentException e) { 
            // Error, multiple courses or invalid URL, error 400
            return ResponseEntity.badRequest().body(
                java.util.Map.of("error", "Kunde inte hämta TimeEdit-data. " + e.getMessage()));
        
        } catch (Exception e) {
            e.printStackTrace();   // for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                java.util.Map.of("error", "Ett oväntat fel inträffade vid hämtning av TimeEdit-data."));
        }

    }

    /* POST Canvas data to calendar 
     * Which calendar it is posted to is determined by inputted context_code
     */
    @PostMapping("/postToCanvas")    // Http endpoint for second button
    public ResponseEntity<PostResult> postToCanvas(@RequestBody List<CalendarEvent> events, 
        @RequestParam String idInputCode) {  

        // Hardcoded context code for testing
        //String contextCode = "user_45559" / "user_168008";   

        System.out.println("Printing mapped events:");   // testing
        System.out.println(events);                        // testing

        try {
            String contextCode = idInputCode;
            List<CanvasCalendarRequest> canvasRequests = canvasService.mapToCanvasCalendar(events, contextCode);
            
            PostResult postResult = canvasService.RESTPostCanvasEvents(canvasRequests);
            
            // Return Status 200 
            return ResponseEntity.ok(postResult); 

        // Catch network or other 500 unexpected runtime errors
        } catch (RuntimeException e) {
            
            // Log the error message to the server console for debugging
            System.err.println("Fel vid  Canvas-överföring: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new PostResult(0, events.size(), events.stream().map(
                    ev -> ev.getActivity()).collect(Collectors.toList())));
            
            // Returnera Status 400 (Bad Request) med felmeddelandet till webbläsaren
            //return ResponseEntity.badRequest().body(
                //"Kunde inte överföra till Canvas: Kontrollera att ID (" + idInputCode + ") är korrekt.");
        }

    }

}