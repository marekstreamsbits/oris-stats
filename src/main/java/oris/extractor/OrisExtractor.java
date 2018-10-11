package oris.extractor;

import oris.extractor.response.DataExtractor;

public interface OrisExtractor<T> {

    String BASE_URL = "https://oris.orientacnisporty.cz/API/?format=json&method=";

    Class<? extends DataExtractor<T>> dataExtractor();
}