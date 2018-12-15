package oris.model.api;

import lombok.Data;

@Data
public class GlobalRivalriesRequestFilter extends PageFilter {

    int minEventCount = 2;

    String category;

    GlobalRivalrySort[] sortBy = {GlobalRivalrySort.EVENT_COUNT, GlobalRivalrySort.WIN_DIFFERENCE};

    enum GlobalRivalrySort {
        EVENT_COUNT("eventCount"),
        WIN_DIFFERENCE("winDifferenceAbs"),;

        final String fieldName;

        GlobalRivalrySort(String fieldName) {
            this.fieldName = fieldName;
        }
    }
}