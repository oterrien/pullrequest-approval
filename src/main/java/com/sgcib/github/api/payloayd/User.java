package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {

    private String login;

    private long id;
}
