package oris.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GlobalRivalryDto implements HasAttendeeRegNo {

    private String attendeeRegNo;

    private String rivalRegNo;

    private String category;

    private int eventCount;

    private int attendeeWinDifference;
}