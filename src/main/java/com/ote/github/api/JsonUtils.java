package com.ote.github.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import javax.validation.constraints.NotNull;
import java.io.IOException;

public final class JsonUtils {

    // ObjectMapper is threadsafe
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtils() {
    }

    public static <T> T parse(@NotNull String content, @NotNull Class<T> type) throws IOException {
        return MAPPER.readValue(content, type);
    }

    public static <T> String serialize(@NotNull T obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
    }
}
