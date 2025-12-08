package com.group8.calendar_integration;
import com.group8.calendar_integration.TimeEditService;
import com.group8.calendar_integration.Model.TimeEditRoot;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller class.
 * For testing, here is the webaddress for this class in timeedit: https://cloud.timeedit.net/ltu/web/schedule1/ri177XQQ750Z50Qv3Q073gZ6y6Y650476Y75Y.html
 */
@RestController
@RequestMapping("/kalender")       // Http beginning for all endpoints in this controller
public class CalendarIntegrationController {

    /* This class currently only fetches TimeEdit calendar data 
     * for development purposes, but it can be changed later to also send
     * calendar data to Canvas at the click of the button
     */
    @GetMapping("/timeEditURL")    // Http endpoint for URL input by user
    public TimeEditRoot showTimeEditData(@RequestParam String url) {
        
        return TimeEditService.fetchCalendar(url);
    }
    

}
