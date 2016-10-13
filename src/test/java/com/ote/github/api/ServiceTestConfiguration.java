package com.ote.github.api;

import com.ote.github.api.component.CommunicationServiceMock;
import com.ote.github.api.component.ICommunicationService;
import com.ote.github.api.component.IRepositoryConfigurationService;
import com.ote.github.api.component.RemoteConfigurationServiceMock;
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
    public IRepositoryConfigurationService remoteConfigurationService() {
        return new RemoteConfigurationServiceMock();
    }
}