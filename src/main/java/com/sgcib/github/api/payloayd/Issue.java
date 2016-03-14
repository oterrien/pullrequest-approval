package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

@Data
public class Issue implements Serializable {

    private String url;

    private int id;

    private PullRequest pullRequest;
}
