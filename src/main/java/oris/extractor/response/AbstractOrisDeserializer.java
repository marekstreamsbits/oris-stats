package oris.extractor.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Field "DATA" when empty is written as an empty array "[]", otherwise it's a classic json object {} which we deserialize as a map.
 */
public abstract class AbstractOrisDeserializer extends JsonDeserializer {

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        if (p.hasCurrentToken() && "[".equals(p.currentToken().asString())) {
            return new HashMap<>();
        }

        return ctxt.readValue(p, javaType());
    }

    protected abstract JavaType javaType();
}