package com.ote.github.api.json;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Users implements Serializable {

    private List<User> users;
}