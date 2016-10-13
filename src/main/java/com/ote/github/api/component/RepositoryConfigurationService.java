package com.ote.github.api.component;

import com.ote.github.api.FilesUtils;
import com.ote.github.api.JsonUtils;
import com.ote.github.api.json.File;
import com.ote.github.api.json.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
            Optional<String> remoteRepositoryContent = getRemoteRepositoryContent(repository);
            return new RepositoryConfiguration(keyConfiguration, remoteRepositoryContent, defaultRepositoryContent);
        } catch (URISyntaxException | IOException e) {
            throw new RepositoryConfigurationException("Unable to initialize configuration for repository '" + repository.getName() + "'", e);
        }
    }

    private Optional<String> getRemoteRepositoryContent(Repository repository) {

        try {
            String defaultBranch = repository.getDefaultBranch();
            String contentsUrl = repository.getContentsUrl();
            contentsUrl = contentsUrl.replace("{+path}", repositoryConfigurationPath + "?ref=" + defaultBranch);
            File file = JsonUtils.parse(communicationService.get(contentsUrl), File.class);
            return Optional.of(communicationService.get(file.getDownloadUrl()));
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("unable to retrieve remote configuration for repository '" + repository.getName() + "' --> use default configuration instead", e.getMessage());
            }
            return Optional.empty();
        }
    }
}
