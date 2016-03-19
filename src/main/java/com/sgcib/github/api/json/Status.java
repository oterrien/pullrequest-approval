package com.sgcib.github.api.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sgcib.github.api.eventhandler.IssueCommentEventHandler;
import com.sgcib.github.api.configuration.Configuration;
import com.sgcib.github.api.configuration.RemoteConfiguration;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

@Data
public class Status implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(IssueCommentEventHandler.class);

    private String state;

    private String description;

    @JsonProperty("target_url")
    private String targetUrl;

    private String context;

    public Status() {

    }

    public Status(State state, String user, Configuration configuration, Optional<RemoteConfiguration> remoteConfiguration) {

        this.context = configuration.getStatusContext();
        this.targetUrl = computeTargetUrl(remoteConfiguration);
        this.state = state.getValue();
        this.description = computeDescription(state, user);
    }

    private static String computeTargetUrl(Optional<RemoteConfiguration> remoteConfiguration) {
        return remoteConfiguration.
                map(conf -> conf.getPayloadUrl()).
                orElse(StringUtils.EMPTY);
    }

    private static String computeDescription(State state, String user) {

        switch (state) {
            case SUCCESS:
                return user + " has approved pull request";
            case PENDING:
                return "pull request is waiting for review";
            case ERROR:
            case FAILURE:
                return user + " has rejected pull request. Please fix it";
            default:
                return state.getValue();
        }
    }

    public enum State {
        SUCCESS("success", "approved"), ERROR("error", "rejected"), PENDING("pending", "pending"), FAILURE("failure", "rejected"), NONE(StringUtils.EMPTY, StringUtils.EMPTY);

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