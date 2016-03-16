package com.sgcib.github.api.eventhandler.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public final class RemoteConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RemoteConfiguration.class);

    private Configuration configuration;

    public RemoteConfiguration(Configuration configuration, String content) throws IOException {
        this.configuration = configuration;
        initProperties(content);
    }

    private Properties properties = new Properties();

    private boolean isAutoApprovalAuthorizedSet;
    private boolean isAutoApprovalAuthorized;

    private boolean isPayloadUrlSet;
    private Optional<String> payloadUrl;

    private void initProperties(String content) throws IOException {
        properties.load(new ByteArrayInputStream(content.getBytes()));
    }

    public boolean isAutoApprovalAuthorized() {

        if (!isAutoApprovalAuthorizedSet) {

            isAutoApprovalAuthorizedSet = true;
            String key = configuration.getRemoteConfigurationAutoApprovalKey();
            try {
                String property = properties.getProperty(key);
                isAutoApprovalAuthorized = Boolean.parseBoolean(property);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to retrieve or parse " + key + "value", e);
                }
                isAutoApprovalAuthorized = false;
            }
        }
        return isAutoApprovalAuthorized;
    }

    public Optional<String> getPayloadUrl() {

        if (!isPayloadUrlSet) {
            isPayloadUrlSet = true;
            String key = configuration.getRemoteConfigurationPayloadUrlKey();
            try {
                String property = properties.getProperty(key);
                payloadUrl = Optional.of(property);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to retrieve or parse " + key + "value", e);
                }
                payloadUrl = Optional.empty();
            }
        }
        return payloadUrl;
    }
}
