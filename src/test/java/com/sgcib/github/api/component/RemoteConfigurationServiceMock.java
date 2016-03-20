package com.sgcib.github.api.component;

import com.sgcib.github.api.FilesUtils;
import com.sgcib.github.api.json.Repository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

@Service
public final class RemoteConfigurationServiceMock implements IRepositoryConfigurationService {

    @Setter
    private Map<String, String> parameters;

    @Value("${repository.configuration.path}")
    private String repositoryConfigurationPath;

    @Autowired
    private IRepositoryConfigurationService.KeyConfiguration keyConfiguration;

    public RepositoryConfiguration createRemoteConfiguration(Repository repository) throws RepositoryConfigurationException {

        try {
            String defaultRepositoryContent = FilesUtils.readFileInClasspath(repositoryConfigurationPath, parameters);
            return new RepositoryConfiguration(keyConfiguration, Optional.empty(), defaultRepositoryContent);
        } catch (URISyntaxException | IOException e) {
            throw new RepositoryConfigurationException("Unable to initialize repository configuration", e);
        }

    }
}
