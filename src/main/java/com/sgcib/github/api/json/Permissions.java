package com.sgcib.github.api.json;

import lombok.Data;

import java.io.Serializable;

@Data
public class Permissions implements Serializable {

    private boolean admin;
}
