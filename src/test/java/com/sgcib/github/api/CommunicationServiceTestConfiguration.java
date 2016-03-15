package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.ICommunicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

@Profile("test")
@Configuration
public class CommunicationServiceTestConfiguration {

    @Bean
    @Primary
    public ICommunicationService communicationService() {

        final Map<String, String> param = new HashMap<>(10);
        param.put("auto_approval.authorized", "true");
        param.put("issue_comment", "approved");
        param.put("last_state", "pending");

        CommunicationServiceMock mock = new CommunicationServiceMock();
        mock.setParameters(param);
        return mock;
    }
}