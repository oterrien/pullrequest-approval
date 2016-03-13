package com.sgcib.github.api.eventhandler;

import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class Status implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(IssueCommentEventHandler.class);

    private String state;

    private String description;

    private String context;

    private String targetUrl;

    public static Optional<Status> findLastStatus(String statusesUrl, String remoteRepositoryName) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String str = restTemplate.getForObject(statusesUrl, String.class);
            str = "{\"statuses\":" + str + "}";
            Statuses statuses = new JSOnService().parse(Statuses.class, str);
            return statuses.getStatuses().stream().filter(p -> p.getContext().equals(State.CONTEXT)).findFirst();
        } catch (RestClientException | IOException e) {
            logger.error(remoteRepositoryName + " : unable to retrieve remote configuration file", e);
            return Optional.empty();
        }
    }

    enum State {
        SUCCESS("success"), ERROR("error"), PENDING("pending"), FAILURE("failure"), NONE("");

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

        private static final String CONTEXT = "manual/pullrequest-approval";

        public Status create() {
            return create(Optional.empty());
        }

        public Status create(String user) {
            return create(Optional.of(user));
        }

        private Status create(Optional<String> user) {
            Status status = new Status();
            status.setContext(CONTEXT);
            status.setTargetUrl("");
            status.setDescription(getDescription(user));
            status.setState(getState());
            return status;
        }
    }
}
