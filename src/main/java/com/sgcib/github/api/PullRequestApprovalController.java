package com.sgcib.github.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sgcib.github.api.component.ICommunicationService;
import com.sgcib.github.api.eventhandler.EventHandlerDispatcher;
import lombok.Data;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public final class PullRequestApprovalController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(PullRequestApprovalController.class);

    @Autowired
    private EventHandlerDispatcher eventHandlerDispatcher;

    @RequestMapping(method = RequestMethod.POST)
    public final ResponseEntity<String> onEvent(@RequestBody String body, @RequestHeader HttpHeaders headers) {

        String event = headers.getFirst("x-github-event");

        checkOAuth();

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Received event type : " + event);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("body : " + body.replaceAll("\n", "").replaceAll("\r", "").replaceAll(" ", ""));
        }

        return eventHandlerDispatcher.handle(event, body);
    }

    private void checkOAuth() {

        try {
            LOGGER.info("Check OAUTH ++++++++++++++++++++++++++++++++++++++++++++++++++++");

            RestTemplate restTemplate = new RestTemplate();

            String clientId = "8f2c34628797c4e697aa";
            String clientSecret = "6f188f2b99f60e90c516e0c98cd3e82b8e33325b";

            String askForCodeUrl = "https://github.com/login/oauth/authorize";

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(askForCodeUrl).
                    queryParam("scope", "repo").
                    queryParam("client_id", clientId);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            HttpEntity<String> response;
            try {
                response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, String.class);
            } catch (Exception e) {
                LOGGER.error("error", e);
            }

            entity.getHeaders();



            // Get https://github.com/login/oauth/authorize?scope=repo&client_id=213722fd939f38841142 to request code
            String code = "d884787b134359e2040c";

            String askForTokenUrl = "https://github.com/login/oauth/access_token";

            builder = UriComponentsBuilder.fromHttpUrl(askForTokenUrl).
                    queryParam("client_secret", clientSecret).
                    queryParam("client_id", clientId).
                    queryParam("code", code);
            entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entity, String.class);
            Token token = JsonUtils.parse(response.getBody(), Token.class);

            String urlWithRight = "https://api.github.com/repos/oterrien/pullrequest-approval-test/collaborators";
            builder = UriComponentsBuilder.fromHttpUrl(urlWithRight).
                    queryParam("access_token", token);
            entity = new HttpEntity<>(headers);
            response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entity, String.class);

            LOGGER.info("Collaborators oterrien/pullrequest-approval-test : "+ response.getBody());
        } catch (Exception e) {
            LOGGER.error("Error while getting token", e);
        } finally {
            LOGGER.info("Check OAUTH ----------------------------------------------------");
        }
    }

    @Data
    public class Token {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("token_type")
        private String tokenType;

        private String scope;
    }
}

