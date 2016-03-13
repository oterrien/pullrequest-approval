package com.sgcib.github.api.eventhandler;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Statuses implements Serializable {

    private List<Status> statuses;
}