package com.sgcib.github.api.service;

import com.sgcib.github.api.FilesUtils;
import com.sgcib.github.api.json.Repository;
import com.sgcib.github.api.service.Configuration;
import com.sgcib.github.api.service.IRemoteConfigurationService;
import com.sgcib.github.api.service.RemoteConfiguration;
import com.sgcib.github.api.service.RemoteConfigurationException;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public final class RemoteConfigurationServiceMock implements IRemoteConfigurationService {

    @Setter
    private Map<String, String> parameters;

    @Autowired
    private Configuration configuration;

    public RemoteConfiguration createRemoteConfiguration(Repository repository) throws RemoteConfigurationException {

        try {
            String content = FilesUtils.readFileInClasspath(configuration.getRemoteConfigurationPath(), parameters);
            return new RemoteConfiguration(configuration, content);
        } catch (Exception e1) {
            throw new RemoteConfigurationException("Unable to initialize default configuration from remote repository nor local", e1);
        }

    }
}
