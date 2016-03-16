package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MocksApplication.class)
@WebAppConfiguration
public class PullRequestApprovalControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CommunicationServiceMock communicationServiceMock;

    private MockMvc mockMvc;

    private final Map<String, String> parameter = new HashMap<>(10);

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @After
    public void cleanup() throws Exception {
        parameter.clear();
    }

    @Test
    public void issueEventComment_approved_should_send_success() throws Exception {

        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", "approved");
        parameter.put("last_state", "pending");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = "issue_comment";

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.SUCCESS.getValue());
    }

    @Test
    public void issueEventComment_auto_approved_should_do_nothing_if_auto_approvement_is_forbidden() throws Exception {

        parameter.put("auto_approval.authorized", "false");
        parameter.put("issue_comment", "approved");
        parameter.put("last_state", "pending");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = "issue_comment";

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.UNAUTHORIZED.toString())));
    }

    @Test
    public void issueEventComment_rejected_should_send_error() throws Exception {

        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", "rejected");
        parameter.put("last_state", "success");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = "issue_comment";

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.ERROR.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.OK.toString())));
    }

    @Test
    public void issueEventComment_tobereviewed_should_send_pending() throws Exception {

        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", "to be reviewed");
        parameter.put("last_state", "success");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = "issue_comment";

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.OK.toString())));
    }

    @Test
    public void pullRequestEvent_created_should_send_pending() throws Exception {

        parameter.put("action", "opened");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("pull-request-event-test.json", parameter);
        String eventType = "pull_request";

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.OK.toString())));
    }

    @Test
    public void pullRequestEvent_synchronized_should_send_pending() throws Exception {

        parameter.put("action", "synchronize");
        parameter.put("last_state", "success");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("pull-request-event-test.json", parameter);
        String eventType = "pull_request";

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.OK.toString())));
    }
}
