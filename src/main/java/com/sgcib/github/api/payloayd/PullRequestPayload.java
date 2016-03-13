package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

@Data
public class PullRequestPayload implements Serializable {

    private String action;

    private int number;

    private PullRequest pullRequest;

    private Repository repository;
}
