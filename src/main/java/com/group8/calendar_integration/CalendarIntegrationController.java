package com.group8.calendar_integration;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group8.calendar_integration.external.CanvasCalendarRequest;
import com.group8.calendar_integration.external.TimeEditRoot;
import com.group8.calendar_integration.Model.CalendarEvent;

import java.util.List;

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

    // Constructor injection of CanvasService (for Spring to manage dependencies)
    public CalendarIntegrationController(CanvasService canvasService) {
        this.canvasService = canvasService;
    }


    /* Fetch TimeEdit calendar data  
     */
    @GetMapping("/timeEditURL")    // Http endpoint for first button
    public List<CalendarEvent> getCanvasEvents(@RequestParam String url) {

        // Fetch TimeEdit data as POJO
        TimeEditRoot calendar = TimeEditService.fetchCalendar(url);

        // Map to Canvas events
        return canvasService.mapToCalendarEvents(calendar.getReservations());

    }

    /* POST Canvas data to calendar 
     * Which calendar it is posted to is determined by inputted context_code
     */
    @PostMapping("/postToCanvas")    // Http endpoint for second button
    public ResponseEntity<String> postToCanvas(@RequestBody List<CalendarEvent> events, 
        @RequestParam String idInputCode) {  

        // Hardcoded context code for testing
        //String contextCode = "user_45559" / "user_168008";   

        System.out.println("Printing mapped events:");   // testing
        System.out.println(events);                        // testing

        // KS additions/modifications on 2026-01-01 begin here ...      
        try {
            String contextCode = idInputCode;
            List<CanvasCalendarRequest> canvasRequests = canvasService.mapToCanvasCalendar(events, contextCode);
            
            String postResult = canvasService.RESTPostCanvasEvents(canvasRequests);
            return ResponseEntity.ok(postResult); // Returnerar Status 200 vid framgång
        
        } catch (RuntimeException e) {
            // Logga felet internt
            System.err.println("Fel vid Canvas-överföring: " + e.getMessage());
            
            // Returnera Status 400 (Bad Request) med felmeddelandet till webbläsaren
            return ResponseEntity.badRequest().body(
                "Kunde inte överföra till Canvas: Kontrollera att ID (" + idInputCode + ") är korrekt.");
        }

    }

}
