package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

@Data
public class Comment implements Serializable {

    private String body;

    private User user;
}
