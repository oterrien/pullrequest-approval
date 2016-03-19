package com.sgcib.github.api.json;

import lombok.Data;

import java.io.Serializable;

@Data
public class IssueCommentEvent implements Serializable {

    private String action;

    private Issue issue;

    private Comment comment;

    private Repository repository;
}
