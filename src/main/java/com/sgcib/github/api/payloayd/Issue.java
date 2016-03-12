package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class Issue implements Serializable {

    private int id;

    private String url;

    private PullRequest pullRequest;
}
