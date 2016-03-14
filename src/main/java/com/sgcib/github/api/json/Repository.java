package com.sgcib.github.api.json;

import lombok.Data;

import java.io.Serializable;

@Data
public class Repository implements Serializable {

    private String name;

    private String pullsUrl;

    private String contentsUrl;

    private String defaultBranch;
}
