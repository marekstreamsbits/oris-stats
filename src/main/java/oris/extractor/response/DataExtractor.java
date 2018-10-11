package oris.extractor.response;

/**
 * Interface helping to extract nice data/data sets from API responses.
 *
 * @param <T> desired result type
 */
public interface DataExtractor<T> {

    T getData();
}