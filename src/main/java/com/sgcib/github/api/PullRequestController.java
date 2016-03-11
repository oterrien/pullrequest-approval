package com.sgcib.github.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Boolean> onEvent(HttpServletRequest request) {

        while(request.getHeaderNames().hasMoreElements()){
            String name = request.getHeaderNames().nextElement();
            System.out.println(name + ":" + request.getHeader(name));
        }


        /*eventFactory.getEventHandler("pull-request").
                ifPresent(h-> h.handle(requestWrapper));
*/
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }
}

