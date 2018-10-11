package oris.extractor.response;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import oris.model.db.EventLite;

import java.util.LinkedHashMap;

public class EventLiteMapDeserializer extends AbstractOrisDeserializer {

    @Override
    protected JavaType javaType() {
        return MapType.construct(LinkedHashMap.class, SimpleType.constructUnsafe(String.class), SimpleType.constructUnsafe(EventLite.class));
    }
}