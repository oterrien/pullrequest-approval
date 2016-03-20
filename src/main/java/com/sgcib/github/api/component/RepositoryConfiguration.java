package com.sgcib.github.api.component;

import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public final class RepositoryConfiguration {

    @Getter
    private boolean isAutoApprovalAuthorized;

    @Getter
    private String payloadUrl;

    @Getter
    private String doNotMergeLabelName;

    Properties repositoryRemoteProperties = new Properties();

    Properties defaultRepositoryProperties = new Properties();

    protected RepositoryConfiguration(IRepositoryConfigurationService.KeyConfiguration keyConfiguration, Optional<String> remoteRepositoryContent, String defaultRepositoryContent) throws IOException {

        if (remoteRepositoryContent.isPresent()) {
            repositoryRemoteProperties.load(new ByteArrayInputStream(remoteRepositoryContent.get().getBytes()));
        }

        this.defaultRepositoryProperties.load(new ByteArrayInputStream(defaultRepositoryContent.getBytes()));

        this.isAutoApprovalAuthorized = Boolean.parseBoolean(get(keyConfiguration.getRepositoryConfigurationAutoApprovalKey()));
        this.payloadUrl = get(keyConfiguration.getRepositoryConfigurationPayloadUrlKey());
        this.doNotMergeLabelName = get(keyConfiguration.getRepositoryConfigurationDoNotMergeLabelKey());
    }

    private String get(String key) {

        if (repositoryRemoteProperties.getProperty(key) != null) {
            return repositoryRemoteProperties.getProperty(key);
        }
        return defaultRepositoryProperties.getProperty(key);
    }
}
