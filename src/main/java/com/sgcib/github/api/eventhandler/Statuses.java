package com.sgcib.github.api.eventhandler;

import lombok.Data;

import java.util.List;

/**
 * Created by Olivier on 11/03/2016.
 */
@Data
public class Statuses {

    private List<Status> statuses;
}