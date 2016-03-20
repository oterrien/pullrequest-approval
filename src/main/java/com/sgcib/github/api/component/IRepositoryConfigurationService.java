package com.sgcib.github.api.component;

import com.sgcib.github.api.json.Repository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public interface IRepositoryConfigurationService {

    RepositoryConfiguration createRemoteConfiguration(Repository repository) throws RepositoryConfigurationException;

    @Component
    class KeyConfiguration {

        @Value("${repository.configuration.key.auto_approval.authorized}")
        @Getter
        private String repositoryConfigurationAutoApprovalKey;

        @Value("${repository.configuration.key.payload.url}")
        @Getter
        private String repositoryConfigurationPayloadUrlKey;

        @Value("${repository.configuration.key.do_not_merge.label}")
        @Getter
        private String repositoryConfigurationDoNotMergeLabelKey;
    }
}
