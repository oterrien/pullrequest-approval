package com.sgcib.github.api.configuration;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

public final class RemoteConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RemoteConfiguration.class);

    @Getter
    private boolean isAutoApprovalAuthorized;

    @Getter
    private String payloadUrl;

    public RemoteConfiguration(Configuration configuration, String content) throws IOException {

        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(content.getBytes()));

        initAutoApprovalAuthorized(configuration, properties);
        initPayloadUrl(configuration, properties);
    }

    public void initAutoApprovalAuthorized(Configuration configuration, Properties properties) {

        String key = configuration.getRemoteConfigurationAutoApprovalKey();
        try {
            isAutoApprovalAuthorized = Boolean.parseBoolean(properties.getProperty(key));
        } catch (Exception e) {
            logError(e, key);
            isAutoApprovalAuthorized = false;
        }
    }

    public void initPayloadUrl(Configuration configuration, Properties properties) {

        String key = configuration.getRemoteConfigurationPayloadUrlKey();
        try {
            payloadUrl = properties.getProperty(key);
        } catch (Exception e) {
            logError(e, key);
            payloadUrl = StringUtils.EMPTY;
        }
    }

    private void logError(Exception e, String key) {
        if (logger.isErrorEnabled()) {
            logger.error("Unable to retrieve or parse value of : " + key, e);
        }
    }
}
