package com.sgcib.github.api.json;

import lombok.Data;

import java.io.Serializable;

@Data
public class File implements Serializable {

    private String downloadUrl;
}
