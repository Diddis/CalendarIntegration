package com.group8.calendar_integration;
import com.group8.calendar_integration.Model.CalendarEvent;
import com.group8.calendar_integration.Model.CanvasCalendarRequest;
import com.group8.calendar_integration.Model.TimeEditRoot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller class.
 * For testing, here is the webaddress for this class in timeedit: https://cloud.timeedit.net/ltu/web/schedule1/ri177XQQ750Z50Qv3Q073gZ6y6Y650476Y75Y.html
 */
@RestController
@RequestMapping("/kalender")       // Http beginning for all endpoints in this controller
public class CalendarIntegrationController {

    /* Fetch TimeEdit calendar data 
     */
    @GetMapping("/timeEditURL")    // Http endpoint for URL input by user
    public List<CalendarEvent> getCanvasEvents(@RequestParam String url) {

        // Fetch TimeEdit data as POJO
        TimeEditRoot calendar = TimeEditService.fetchCalendar(url);

        // Map to Canvas events
        
        return CanvasService.mapToCalendarEvents(calendar.getReservations());
    }

    /* POST Canvas data to calendar related to inputted context_code
     */
    


    

}
