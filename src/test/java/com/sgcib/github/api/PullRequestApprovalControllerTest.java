package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.Status;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MocksApplication.class)
@WebAppConfiguration
public class PullRequestApprovalControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CommunicationServiceMock communicationService;

    private Map<String, String> parameters;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        parameters = new HashMap<>(10);
        parameters.put("auto_approval.authorized", "true");
        parameters.put("issue_comment", "approved");
        parameters.put("last_state", "pending");
    }

    @Test
    public void issueEventComment_approved_ShouldPostSuccessStatus() throws Exception {

        String content = TestUtils.readFile("issue-comment-event-test.json", parameters);
        String eventType = "issue_comment";

        // Simulate a calling of webservice
        mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationService.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.SUCCESS.getState());
    }
}
