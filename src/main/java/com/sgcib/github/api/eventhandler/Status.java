package com.sgcib.github.api.eventhandler;

import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Optional;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class Status implements Serializable {

    private String state;

    private String description;

    private String context;

    private String targetUrl;

    enum State {
        SUCCESS("success"), ERROR("error"), PENDING("pending"), FAILURE("failure");

        @Getter
        private String state;

        private String description;

        State(String state) {
            this.state = state;
        }

        public String getDescription(Optional<String> user) {

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
    }
}
