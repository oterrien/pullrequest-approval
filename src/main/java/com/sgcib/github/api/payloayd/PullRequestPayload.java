package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class PullRequestPayload implements Serializable {

    private String action;

    private int number;

    private PullRequest pullRequest;

    private Repository repository;
}
