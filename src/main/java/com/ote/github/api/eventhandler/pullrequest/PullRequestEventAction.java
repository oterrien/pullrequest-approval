package com.ote.github.api.eventhandler.pullrequest;

import lombok.Getter;

import java.util.stream.Stream;

public enum PullRequestEventAction {

    CLOSED("closed"),
    OPENED("opened"),
    REOPENED("reopened"),
    SYNCHRONIZED("synchronize"),
    LABELED("labeled"),
    UNLABELED("unlabeled"),
    NONE("");

    @Getter
    private String value;

    PullRequestEventAction(String value) {
        this.value = value;
    }

    public static PullRequestEventAction of(final String value) {
        return Stream.of(PullRequestEventAction.values()).
                filter(p -> p.value.equals(value)).
                findFirst().
                orElse(NONE);
    }
}
