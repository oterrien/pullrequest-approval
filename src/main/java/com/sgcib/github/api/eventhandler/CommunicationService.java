package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.eventhandler.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class CommunicationService implements ICommunicationService{

    private static final Logger logger = LoggerFactory.getLogger(CommunicationService.class);

    protected Configuration handlerConfiguration;

    protected RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public CommunicationService(Configuration handlerConfiguration) {
        this.handlerConfiguration = handlerConfiguration;
    }

    @Override
    public <T> HttpStatus post(String url, T object) throws EventHandlerException {

        try {
            if (logger.isInfoEnabled()) {
                logger.info("Posting " + object.toString() + " to " + url);
            }

            restTemplate.
                    postForObject(url, new HttpEntity<>(JsonUtils.serialize(object), this.handlerConfiguration.getHttpHeaders()), String.class);

            return HttpStatus.OK;

        } catch (IOException | RestClientException e) {
            // TODO change exception to CommunicationException
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST, "Error while posting data to " + url, object.toString());
        }
    }

    @Override
    public String get(String url) throws EventHandlerException {

        if (logger.isInfoEnabled()) {
            logger.info("Getting data from " + url);
        }

        try {
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            // TODO change exception to CommunicationException
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST, "Error while retrieving data from " + url);
        }
    }

    @Override
    public <T> T get(String url, Class<T> type) throws EventHandlerException {

        String result = get(url);
        try {
            return JsonUtils.parse(result, type);
        } catch (IOException e) {
            // TODO change exception to CommunicationException
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while parsing result from " + url, result);
        }
    }
}
