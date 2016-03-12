package com.sgcib.github.api.payloayd;

import lombok.Data;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class Status {

    private String state;

    private String targetUrl;

    private String description;

    private String context;

}
