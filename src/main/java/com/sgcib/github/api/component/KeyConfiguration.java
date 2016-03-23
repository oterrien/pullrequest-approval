package com.sgcib.github.api.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyConfiguration {

    @Value("${repository.configuration.key.auto_approval-authorized}")
    @Getter
    private String repositoryConfigurationAutoApprovalKey;

    @Value("${repository.configuration.key.do_not_merge-label}")
    @Getter
    private String repositoryConfigurationDoNotMergeLabelKey;

    @Value("${repository.configuration.key.admins-team}")
    @Getter
    private String repositoryConfigurationAdminTeamKey;
}