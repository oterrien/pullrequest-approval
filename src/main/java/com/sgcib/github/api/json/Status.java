package com.sgcib.github.api.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.stream.Stream;

@Data
public class Status implements Serializable {

    private String state;

    private String description;

    @JsonProperty("target_url")
    private String targetUrl;

    private String context;

    public enum State {
        SUCCESS("success", "approved"),
        ERROR("error", "rejected"),
        PENDING("pending", "pending"),
        FAILURE("failure", "rejected"),
        NONE(StringUtils.EMPTY, StringUtils.EMPTY);

        @Getter
        private String value;

        @Getter
        private String description;

        State(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public static State of(final String state) {
            return Stream.of(State.values()).
                    filter(p -> p.value.equals(state)).
                    findFirst().
                    orElse(NONE);
        }
    }
}