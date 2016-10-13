package com.ote.github.api.json;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Issue implements Serializable {

    private String url;

    private int id;

    private User user;

    private PullRequest pullRequest;

    private List<Label> labels;
}
