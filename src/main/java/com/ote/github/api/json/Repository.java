package com.ote.github.api.json;

import lombok.Data;

import java.io.Serializable;

@Data
public class Repository implements Serializable {

    private String name;

    private User owner;

    private String pullsUrl;

    private String contentsUrl;

    private String collaboratorsUrl;

    private String defaultBranch;
}
