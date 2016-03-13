package com.sgcib.github.api.eventhandler;

import lombok.Data;

import java.io.Serializable;

@Data
public class File implements Serializable {

    private String downloadUrl;
}
