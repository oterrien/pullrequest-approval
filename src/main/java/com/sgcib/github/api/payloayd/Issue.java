package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

@Data
public class Issue implements Serializable {

    private int id;

    private String url;

    private PullRequest pullRequest;
}
