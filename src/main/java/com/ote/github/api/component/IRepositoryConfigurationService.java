package com.ote.github.api.component;

import com.ote.github.api.json.Repository;

public interface IRepositoryConfigurationService {

    RepositoryConfiguration createRemoteConfiguration(Repository repository) throws RepositoryConfigurationException;
}
