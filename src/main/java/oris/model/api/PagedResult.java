package oris.model.api;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PagedResult<T> {

    private final int total;

    private final int pageNumber;

    private final int pagesTotal;

    private final List<T> data;
}