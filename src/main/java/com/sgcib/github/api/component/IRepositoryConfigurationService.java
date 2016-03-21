package com.sgcib.github.api.component;

import com.sgcib.github.api.json.Repository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public interface IRepositoryConfigurationService {

    RepositoryConfiguration createRemoteConfiguration(Repository repository) throws RepositoryConfigurationException;
}
