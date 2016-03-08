package com.sgcib.github.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by Olivier on 18/02/2016.
 */
@RestController
@SpringBootApplication
public class PullRequestController {

    @Autowired
    private EventFactory eventFactory;

    public static void main(String[] args) {
        SpringApplication.run(PullRequestController.class, args);
    }
    @RequestMapping(method = RequestMethod.POST, name = "/pullrequest-approval")
    public ResponseEntity<Boolean> onEvent(@RequestBody String requestWrapper) {

        //System.out.println(requestWrapper);

        eventFactory.handle("pull-request1");

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);

    }

    /*@RequestMapping(method = RequestMethod.POST, name = "/pullrequest-approval")
    public ResponseEntity<Boolean> onEvent2(HttpRequest requestWrapper) {

        System.out.println(requestWrapper.getHeaders());

        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }*/

}

