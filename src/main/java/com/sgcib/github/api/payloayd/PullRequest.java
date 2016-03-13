package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

@Data
public class PullRequest implements Serializable {

    private int id;

    private String url;

    private String statusesUrl;

    private User user;

}
