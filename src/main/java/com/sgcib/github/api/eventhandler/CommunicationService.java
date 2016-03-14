package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.JsonService;
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
public class CommunicationService {

    private static final Logger logger = LoggerFactory.getLogger(CommunicationService.class);

    @Autowired
    protected Configuration handlerConfiguration;

    @Autowired
    protected JsonService jsonService;

    protected RestTemplate restTemplate = new RestTemplate();

    public <T> HttpStatus post(String url, T object, String remoteRepositoryName) throws EventHandlerException {

        try {
            if (logger.isInfoEnabled()) {
                logger.info(remoteRepositoryName + " : posting " + object.toString() + " to " + url);
            }

            restTemplate.
                    postForObject(url, new HttpEntity<>(object, this.handlerConfiguration.getHttpHeaders()), String.class);

            return HttpStatus.OK;

        } catch (RestClientException e) {
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST,
                    remoteRepositoryName + " : error while posting data to " + url, object.toString());
        }
    }

    public String get(String url, String remoteRepositoryName) throws EventHandlerException {

        if (logger.isInfoEnabled()) {
            logger.info(remoteRepositoryName + " : getting data from " + url);
        }

        try {
            return restTemplate.getForObject(url, String.class);
        } catch (RestClientException e) {
            throw new EventHandlerException(e, HttpStatus.BAD_REQUEST,
                    remoteRepositoryName + " : error while retrieving data from " + url);
        }
    }

    public <T> T get(String url, String remoteRepositoryName, Class<T> clazz) throws EventHandlerException {

        String result = get(url, remoteRepositoryName);
        try {
            return jsonService.parse(result, clazz);
        } catch (IOException e) {
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY,
                    remoteRepositoryName + " : error while parsing result from " + url, result);
        }
    }
}
