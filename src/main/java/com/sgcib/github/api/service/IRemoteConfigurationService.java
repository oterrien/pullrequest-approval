package com.sgcib.github.api.service;

import com.sgcib.github.api.json.Repository;

public interface IRemoteConfigurationService {

    RemoteConfiguration createRemoteConfiguration(Repository repository) throws RemoteConfigurationException;
}
