package com.sgcib.github.api.service;

import com.sgcib.github.api.service.Configuration;
import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

public final class RemoteConfiguration {

    @Getter
    private boolean isAutoApprovalAuthorized;

    @Getter
    private String payloadUrl;

    @Getter
    private String doNotMergeLabelName;

    protected RemoteConfiguration(Configuration configuration, String content) throws IOException {

        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(content.getBytes()));

        this.isAutoApprovalAuthorized = Boolean.parseBoolean(properties.getProperty(configuration.getRemoteConfigurationAutoApprovalKey()));
        this.payloadUrl = properties.getProperty(configuration.getRemoteConfigurationPayloadUrlKey());
        this.doNotMergeLabelName = properties.getProperty(configuration.getRemoteConfigurationDoNotMergeLabelKey());
    }
}
