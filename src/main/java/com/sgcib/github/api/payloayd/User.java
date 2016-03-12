package com.sgcib.github.api.payloayd;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Olivier on 12/03/2016.
 */
@Data
public class User implements Serializable {

    public String login;
}
