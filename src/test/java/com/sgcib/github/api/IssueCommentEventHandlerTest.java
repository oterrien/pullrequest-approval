package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.CommunicationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class IssueCommentEventHandlerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EventFactory eventFactoryMock;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void issueEventComment_approved_ShouldPostSuccessStatus() throws Exception {

        String content = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("issue_comment_event_4UT.json").toURI())));

        String eventType = "issue_comment";

        when(eventFactoryMock.getEventHandler(eventType)).thenReturn(Optional.empty());


        mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType));


    }

    private String mockGetPulls() throws Exception {
        return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("pull_request_event_4UT.json").toURI())));
    }
}
