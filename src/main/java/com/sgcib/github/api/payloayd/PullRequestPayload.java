package com.sgcib.github.api.payloayd;

import lombok.Data;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class PullRequestPayload {

    private String action;

    private int number;

    private PullRequest pullRequest;
}
