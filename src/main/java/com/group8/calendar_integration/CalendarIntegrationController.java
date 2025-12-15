package com.group8.calendar_integration;
import com.group8.calendar_integration.Model.CalendarEvent;
import com.group8.calendar_integration.Model.CanvasCalendarRequest;
import com.group8.calendar_integration.Model.TimeEditRoot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller class.
 * For testing, here is the webaddress for this class in timeedit: https://cloud.timeedit.net/ltu/web/schedule1/ri177XQQ750Z50Qv3Q073gZ6y6Y650476Y75Y.html
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

    /* POST Canvas data to calendar related to inputted context_code
     */
    @PostMapping("/postToCanvas")    // Http endpoint for second button
    public String postToCanvas(@RequestBody List<CalendarEvent> events) {

        // Convert CalendarEvents to CanvasCalendarRequest format
        String contextCode = "user_45559";    // for testing, need to add a @RequestParam later in the method

        System.out.println("Printing mapped events:");
        System.out.println(events);

        List<CanvasCalendarRequest> canvasRequests = canvasService.mapToCanvasCalendar(
            events, contextCode);

        // Post events to Canvas calendar
        String postResult = canvasService.RESTPostCanvasEvents(canvasRequests);

        return postResult;

    }

}
