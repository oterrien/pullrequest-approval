package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.PullRequestEventHandler;
import com.sgcib.github.api.eventhandler.configuration.Configuration;
import com.sgcib.github.api.json.Comment;
import com.sgcib.github.api.json.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private Configuration configuration;

    @Autowired
    private CommunicationServiceMock communicationServiceMock;

    private MockMvc mockMvc;

    private final Map<String, String> parameter = new HashMap<>(10);

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        communicationServiceMock.setPostedStatus(null);
        communicationServiceMock.setPostedComment(null);
    }

    @After
    public void cleanup() throws Exception {
        parameter.clear();
    }

    @Test
    public void issueEventComment_approved_should_send_success_when_auto_approval_is_authorized() throws Exception {

        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", configuration.getApprovalCommentsList().get(0));
        parameter.put("last_state", Status.State.PENDING.getValue());
        parameter.put("user", "my_owner");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.ISSUE_COMMENT.getValue();

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.SUCCESS.getValue());
    }

    @Test
    public void issueEventComment_approved_should_send_error_and_post_comment_when_auto_approval_is_forbidden() throws Exception {

        parameter.put("auto_approval.authorized", "false");
        parameter.put("issue_comment", configuration.getApprovalCommentsList().get(0));
        parameter.put("last_state", Status.State.PENDING.getValue());
        parameter.put("user", "my_owner");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.ISSUE_COMMENT.getValue();

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNull();

        Comment comment = communicationServiceMock.getPostedComment();
        assertThat(comment).isNotNull();
        assertThat(comment.getBody()).isNotEmpty();
    }

    @Test
    public void issueEventComment_auto_approved_should_send_success_and_post_comment_when_auto_approval_is_enabled() throws Exception {

        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", configuration.getAutoApprovalCommentsList().get(0) + " because I was alone");
        parameter.put("last_state", Status.State.PENDING.getValue());
        parameter.put("user", "my_owner");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.ISSUE_COMMENT.getValue();

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.SUCCESS.getValue());

        Comment comment = communicationServiceMock.getPostedComment();
        assertThat(comment).isNotNull();
        assertThat(comment.getBody()).isNotEmpty();
        assertThat(comment.getBody()).contains("because I was alone");
    }

    @Test
    public void issueEventComment_rejected_should_send_error() throws Exception {

        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", configuration.getRejectionCommentsList().get(0));
        parameter.put("last_state", Status.State.SUCCESS.getValue());
        parameter.put("user", "my_owner");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.ISSUE_COMMENT.getValue();

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.ERROR.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.OK.toString())));
    }

    @Test
    public void issueEventComment_tobereviewed_should_send_pending() throws Exception {

        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", configuration.getPendingCommentsList().get(0));
        parameter.put("last_state", Status.State.SUCCESS.getValue());
        parameter.put("user", "my_owner");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.ISSUE_COMMENT.getValue();

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.OK.toString())));
    }

    @Test
    public void pullRequestEvent_created_should_send_pending() throws Exception {

        parameter.put("action", PullRequestEventHandler.Action.OPENED.getValue());

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("pull-request-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.PULL_REQUEST.getValue();

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.OK.toString())));
    }

    @Test
    public void pullRequestEvent_synchronized_should_send_pending() throws Exception {

        parameter.put("action", PullRequestEventHandler.Action.SYNCHRONIZED.getValue());
        parameter.put("last_state", Status.State.SUCCESS.getValue());

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("pull-request-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.PULL_REQUEST.getValue();

        // Simulate a calling of webservice
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(result.andExpect(mvcResult -> Objects.equals(mvcResult.getResponse().getContentAsString(), HttpStatus.OK.toString())));
    }

}
