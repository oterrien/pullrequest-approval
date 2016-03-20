package com.sgcib.github.api;

import com.sgcib.github.api.service.CommunicationServiceMock;
import com.sgcib.github.api.service.ICommunicationService;
import com.sgcib.github.api.service.IRemoteConfigurationService;
import com.sgcib.github.api.service.RemoteConfigurationServiceMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class ServiceTestConfiguration {

    @Bean
    @Primary
    public ICommunicationService communicationService() {
        return new CommunicationServiceMock();
    }

    @Bean
    @Primary
    public IRemoteConfigurationService remoteConfigurationService() {
        return new RemoteConfigurationServiceMock();
    }
}