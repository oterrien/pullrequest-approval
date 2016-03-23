package com.sgcib.github.api.component;

import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.eventhandler.EventHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class CommunicationService implements ICommunicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationService.class);

    @Autowired
    protected AuthorizationConfiguration authorizationConfiguration;

    protected RestTemplate restTemplate = new RestTemplate();

    @Override
    public <T> HttpStatus post(String url, T object) {

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Posting " + object.toString() + " to " + url);
            }

            HttpEntity<String> entity = new HttpEntity<>(JsonUtils.serialize(object), authorizationConfiguration.getHttpHeaders());
            restTemplate.postForObject(url, entity, String.class);

            return HttpStatus.OK;

        } catch (IOException | RestClientException e) {
            // TODO change exception to CommunicationException
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST, "Error while posting data to " + url);
        }
    }

    @Override
    public String get(String url) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Getting data from " + url);
        }

        try {
            HttpEntity<String> entity = new HttpEntity<>(authorizationConfiguration.getHttpHeaders());
            return restTemplate.exchange(new URI(url), HttpMethod.GET, (HttpEntity<?>) entity, String.class).getBody();
        } catch (URISyntaxException | RestClientException e) {
            // TODO change exception to CommunicationException
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST, "Error while retrieving data from " + url);
        }
    }

    @Override
    public <T> T get(String url, Class<T> type) {

        String result = get(url);
        try {
            return JsonUtils.parse(result, type);
        } catch (IOException e) {
            // TODO change exception to CommunicationException
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while parsing result from " + url);
        }
    }
}
