package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.CommunicationService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class CommunicationServiceTestConfiguration {

    @Bean
    @Primary
    public CommunicationService nameService() {
        return Mockito.mock(CommunicationService.class);
    }
}