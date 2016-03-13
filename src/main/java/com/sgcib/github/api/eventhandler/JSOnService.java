package com.sgcib.github.api.eventhandler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public final class JsonService {

    // ObjectMapper is threadsafe
    private final ObjectMapper mapper;

    public JsonService() {
        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public <T> T parse(String content, Class<T> type) throws IOException {
        return this.mapper.readValue(content, type);
    }

    public <T> String serialize(T obj) throws IOException {
        return this.mapper.writeValueAsString(obj);
    }
}
