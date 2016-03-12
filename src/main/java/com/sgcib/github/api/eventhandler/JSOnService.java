package com.sgcib.github.api.eventhandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by Olivier on 11/03/2016.
 */
@Service
public final class JSOnService {

    private final ObjectMapper mapper;

    public JSOnService() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T> T parse(Class<T> type, String content) throws IOException {
        return mapper.readValue(content, type);
    }

    public <T> String serialize(T obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }
}
