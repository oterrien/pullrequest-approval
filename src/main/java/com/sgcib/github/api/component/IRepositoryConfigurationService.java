package com.sgcib.github.api.component;

import com.sgcib.github.api.json.Repository;

public interface IRepositoryConfigurationService {

    RepositoryConfiguration createRemoteConfiguration(Repository repository) throws RepositoryConfigurationException;
}
