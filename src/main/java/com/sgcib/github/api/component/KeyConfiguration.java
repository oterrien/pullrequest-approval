package com.sgcib.github.api.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyConfiguration {

    @Value("${repository.configuration.key.auto_approval.is_authorized}")
    @Getter
    private String autoApprovalAuthorizedKey;

    @Value("${repository.configuration.key.do_not_merge.label.name}")
    @Getter
    private String doNotMergeLabelNameKey;

    @Value("${repository.configuration.key.admins.teams.name}")
    @Getter
    private String adminsTeamsNameKey;
}