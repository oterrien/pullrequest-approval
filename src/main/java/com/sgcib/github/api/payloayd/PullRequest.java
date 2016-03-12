package com.sgcib.github.api.payloayd;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class PullRequest {

    private int id;

    private String url;

    private String statusesUrl;

}
