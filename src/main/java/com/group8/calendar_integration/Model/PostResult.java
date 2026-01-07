package com.group8.calendar_integration.model;

import java.util.List;

/**
 * Java object class for result of post successes and failures.  
 */
public class PostResult {
    
    private int successCount;
    private int failureCount;
    private List<String> failedEvents;

    public PostResult(int successCount, int failureCount, List<String> failedEvents) {
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.failedEvents = failedEvents;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public List<String> getFailedEvents() {
        return failedEvents;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public void setFailedEvents(List<String> failedEvents) {
        this.failedEvents = failedEvents;
    }

    @Override
    public String toString() {
        return "PostResult [successCount=" + successCount + ", failureCount=" + failureCount 
                + ", failedEvents=" + failedEvents + "]";
    }
}