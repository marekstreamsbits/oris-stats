package oris.model.api;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AttendeeStatistics {

    private String regNo;
    private List<EventResult> eventResults = new ArrayList<>();
}