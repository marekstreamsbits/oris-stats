package oris.model.api;

import java.util.ArrayList;
import java.util.List;

public class AttendeeStatistics {

    private String regNo;
    private List<EventResult> eventResults = new ArrayList<>();

    public List<EventResult> getEventResults() {
        return eventResults;
    }

    public void setEventResults(List<EventResult> eventResults) {
        this.eventResults = eventResults;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }
}