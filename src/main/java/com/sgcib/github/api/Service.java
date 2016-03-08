package com.sgcib.github.api;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created by Olivier on 08/03/2016.
 */
@Component
public class Service {

    void display(String param){
        System.out.println(param);
    }
}
