package com.sgcib.github.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public final class JsonService<T> {

    // ObjectMapper is threadsafe
    private final ObjectMapper mapper;

    private final Class<T> typeRef;

    public JsonService() {

        this.typeRef = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        this.mapper = new ObjectMapper();
        this.mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public T parse(String content, Class<T> typeRef) throws IOException {
        return mapper.readValue(content, typeRef);
    }

    public T parse(String content) throws IOException {
        return parse(content, typeRef);
    }

    public String serialize(T obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }
}
