package com.sgcib.github.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;


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

    @RequestMapping(method = RequestMethod.POST, name = "/")
    public ResponseEntity<Boolean> onEvent(RequestEntity request) {

        request.getHeaders().entrySet().stream().forEach(p -> System.out.println(p.getKey() + " : " + p.getValue()));

        System.out.println(request.getBody());


        /*eventFactory.getEventHandler("pull-request").
                ifPresent(h-> h.handle(requestWrapper));
*/
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }
}

