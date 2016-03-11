package com.sgcib.github.api.payloayd;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class IssueComment {

    private String action;

    private Issue issue;

    private Comment comment;

    private Repository repository;
}
