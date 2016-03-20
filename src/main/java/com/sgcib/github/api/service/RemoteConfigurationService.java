package com.sgcib.github.api.service;

import com.sgcib.github.api.FilesUtils;
import com.sgcib.github.api.JsonUtils;
import com.sgcib.github.api.json.File;
import com.sgcib.github.api.json.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class RemoteConfigurationService implements IRemoteConfigurationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConfigurationService.class);

    private Configuration configuration;

    private ICommunicationService communicationService;

    @Autowired
    public RemoteConfigurationService(Configuration configuration, ICommunicationService communicationService) {
        this.configuration = configuration;
        this.communicationService = communicationService;
    }

    public RemoteConfiguration createRemoteConfiguration(Repository repository) throws RemoteConfigurationException {
        try {
            return getRemoteConfigurationFromRemote(repository);
        } catch (Exception e) {
            try {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Unable to initialize remote configuration --> trying to load default configuration from local", e);
                }
                return getRemoteConfigurationFromLocal();
            } catch (Exception e1) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Unable to initialize default configuration from local", e1);
                }
                e1.initCause(e);
                throw new RemoteConfigurationException("Unable to initialize default configuration from remote repository nor local", e1);
            }
        }
    }

    private RemoteConfiguration getRemoteConfigurationFromRemote(Repository repository) throws Exception {

        String defaultBranch = repository.getDefaultBranch();
        String contentsUrl = repository.getContentsUrl();
        contentsUrl = contentsUrl.replace("{+path}", configuration.getRemoteConfigurationPath() + "?ref=" + defaultBranch);
        File file = JsonUtils.parse(communicationService.get(contentsUrl), File.class);
        String content = communicationService.get(file.getDownloadUrl());
        return new RemoteConfiguration(configuration, content);
    }

    private RemoteConfiguration getRemoteConfigurationFromLocal() throws Exception {

        String content = FilesUtils.readFileInClasspath(configuration.getRemoteConfigurationPath());
        return new RemoteConfiguration(configuration, content);
    }


}
