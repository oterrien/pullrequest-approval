package com.ote.github.api.component;

import com.ote.github.api.FilesUtils;
import com.ote.github.api.component.IRepositoryConfigurationService;
import com.ote.github.api.component.KeyConfiguration;
import com.ote.github.api.component.RepositoryConfiguration;
import com.ote.github.api.component.RepositoryConfigurationException;
import com.ote.github.api.json.Repository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

@Service
public class RemoteConfigurationServiceMock implements IRepositoryConfigurationService {

    @Setter
    private Map<String, String> parameters;

    @Value("${repository.configuration.path}")
    private String repositoryConfigurationPath;

    @Autowired
    private KeyConfiguration keyConfiguration;

    public RepositoryConfiguration createRemoteConfiguration(Repository repository) throws RepositoryConfigurationException {

        try {
            String defaultRepositoryContent = FilesUtils.readFileInClasspath(repositoryConfigurationPath, parameters);
            return new RepositoryConfiguration(keyConfiguration, Optional.empty(), defaultRepositoryContent);
        } catch (URISyntaxException | IOException e) {
            throw new RepositoryConfigurationException("Unable to initialize repository configuration", e);
        }

    }
}
