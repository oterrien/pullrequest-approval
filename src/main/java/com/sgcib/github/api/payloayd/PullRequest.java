package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class PullRequest implements Serializable {

    private int id;

    private String url;

    private String statusesUrl;

    private User user;

}
