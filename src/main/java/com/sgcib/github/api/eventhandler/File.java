package com.sgcib.github.api.eventhandler;

import lombok.Getter;
import lombok.Setter;

import java.util.Base64;

/**
 * Created by Olivier on 12/03/2016.
 */
public class File {

    @Setter
    private String content;

    @Getter
    @Setter
    private String downloadUrl;

    public String getContent() {

        return new String(Base64.getDecoder().decode(content));
    }
}
