package com.sgcib.github.api.component;

import com.sgcib.github.api.FilesUtils;
import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.json.File;
import com.sgcib.github.api.json.Repository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

@Service
public final class RepositoryConfigurationService implements IRepositoryConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryConfigurationService.class);

    private ICommunicationService communicationService;

    @Value("${repository.configuration.path}")
    private String repositoryConfigurationPath;

    @Autowired
    private KeyConfiguration keyConfiguration;

    @Autowired
    public RepositoryConfigurationService(ICommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    public RepositoryConfiguration createRemoteConfiguration(Repository repository) throws RepositoryConfigurationException {

        try {
            String defaultRepositoryContent = FilesUtils.readFileInClasspath(repositoryConfigurationPath);
            String remoteRepositoryContent = getRemoteRepositoryContent(repository);
            return new RepositoryConfiguration(keyConfiguration, Optional.of(remoteRepositoryContent), defaultRepositoryContent);
        } catch (URISyntaxException | IOException e) {
            throw new RepositoryConfigurationException("Unable to initialize repository configuration", e);
        }
    }

    private String getRemoteRepositoryContent(Repository repository) throws IOException {
        String defaultBranch = repository.getDefaultBranch();
        String contentsUrl = repository.getContentsUrl();
        contentsUrl = contentsUrl.replace("{+path}", repositoryConfigurationPath + "?ref=" + defaultBranch);
        File file = JsonUtils.parse(communicationService.get(contentsUrl), File.class);
        return communicationService.get(file.getDownloadUrl());
    }
}
