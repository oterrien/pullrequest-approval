package com.sgcib.github.api.eventhandler.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public final class RemoteConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RemoteConfiguration.class);

    private static final String AUTO_APPROVAL_AUTHORIZED = "auto_approval.authorized";

    private Properties properties;

    private boolean isAutoApprovalAuthorized, isAutoApprovalAuthorizedSet;

    public RemoteConfiguration(Properties properties) {
        this.properties = properties;
    }

    public boolean isAutoApprovalAuthorized() {

        if (!isAutoApprovalAuthorizedSet) {

            isAutoApprovalAuthorizedSet = true;
            try {
                String property = properties.getProperty(AUTO_APPROVAL_AUTHORIZED);
                isAutoApprovalAuthorized = Boolean.parseBoolean(property);
            } catch (Exception e) {
                logger.error("Unable to retrieve or parse " + AUTO_APPROVAL_AUTHORIZED + "value");
            }

        }

        return isAutoApprovalAuthorized;
    }
}
