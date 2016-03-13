package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

@Data
public class IssueCommentPayload implements Serializable {

    private String action;

    private Issue issue;

    private Comment comment;

    private Repository repository;
}
