package com.sgcib.github.api.component;

import lombok.Getter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public final class RepositoryConfiguration {

    @Getter
    private final boolean isAutoApprovalAuthorized;

    @Getter
    private final String doNotMergeLabelName;

    @Getter
    private final List<String> adminTeam;

    Properties repositoryRemoteProperties = new Properties();

    Properties defaultRepositoryProperties = new Properties();

    protected RepositoryConfiguration(KeyConfiguration keyConfiguration, Optional<String> remoteRepositoryContent, String defaultRepositoryContent) throws IOException {

        if (remoteRepositoryContent.isPresent()) {
            repositoryRemoteProperties.load(new ByteArrayInputStream(remoteRepositoryContent.get().getBytes()));
        }

        this.defaultRepositoryProperties.load(new ByteArrayInputStream(defaultRepositoryContent.getBytes()));

        this.isAutoApprovalAuthorized = Boolean.parseBoolean(get(keyConfiguration.getRepositoryConfigurationAutoApprovalKey()));
        this.doNotMergeLabelName = get(keyConfiguration.getRepositoryConfigurationDoNotMergeLabelKey());
        this.adminTeam= Arrays.asList(get(keyConfiguration.getRepositoryConfigurationAdminTeamKey()).split(","));
    }

    private String get(String key) {

        if (repositoryRemoteProperties.getProperty(key) != null) {
            return repositoryRemoteProperties.getProperty(key);
        }
        return defaultRepositoryProperties.getProperty(key);
    }
}
