package com.sgcib.github.api.eventhandler;

import lombok.Data;
import lombok.Getter;
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

    private String targetUrl;

    private String context;

    public enum State {
        SUCCESS("success"), ERROR("error"), PENDING("pending"), FAILURE("failure"), NONE("");

        public static final String CONTEXT = "manual/pullrequest-approval";

        @Getter
        private String state;

        private String description;

        State(String state) {
            this.state = state;
        }

        private String getDescription(Optional<String> user) {

            if (description == null) {
                switch (this) {
                    case SUCCESS:
                        this.description = user.isPresent() ? user.get() + " has approved Pull request" : "Pull request has been approved";
                        break;
                    case PENDING:
                        this.description = "Pull request is waiting for review";
                        break;
                    case ERROR:
                    case FAILURE:
                        this.description = user.isPresent() ? user.get() + " has rejected Pull request. Please fix it" : "Pull request has been rejected";
                        break;
                }
            }
            return description;
        }

        public static State of(final String state) {
            return Stream.of(State.values()).
                    filter(p -> p.state.equals(state)).
                    findFirst().
                    orElse(NONE);
        }

        public Status createStatus() {
            return createStatus(Optional.empty());
        }

        public Status createStatus(String user) {
            return createStatus(Optional.of(user));
        }

        private Status createStatus(Optional<String> user) {
            Status status = new Status();
            status.setContext(CONTEXT);
            status.setTargetUrl("");
            status.setDescription(getDescription(user));
            status.setState(getState());
            return status;
        }
    }
}
