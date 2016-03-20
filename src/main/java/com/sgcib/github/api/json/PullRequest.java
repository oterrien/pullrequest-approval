package com.sgcib.github.api.json;

import lombok.Data;

import java.io.Serializable;

@Data
public class PullRequest implements Serializable {

    private String url;

    private int id;

    private User user;

    private String issueUrl;

    private String statusesUrl;

    private String commentsUrl;
}
