package oris.extractor;

import java.util.Collection;
import java.util.List;

public abstract class BaseOrisExtractor<T> implements OrisExtractor<T> {

    public String url() {
        return BASE_URL + method() + buildParams(params());
    }

    private String buildParams(Collection<String> params) {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String param : params) {
            stringBuilder.append("&");
            stringBuilder.append(param);
        }
        return stringBuilder.toString();
    }

    protected void addIfNotNull(String key, String value, List<String> params) {
        if (value != null) {
            params.add(key + "=" + value);
        }
    }

    abstract protected String method();

    /**
     * Format for a param is "param=value"
     */
    abstract protected Collection<String> params();
}