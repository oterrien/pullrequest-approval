package com.sgcib.github.api;

import com.sgcib.github.api.component.*;
import com.sgcib.github.api.eventhandler.EventHandlerDispatcher;
import com.sgcib.github.api.eventhandler.pullrequest.PullRequestEventAction;
import com.sgcib.github.api.json.Comment;
import com.sgcib.github.api.json.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MocksApplication.class)
@WebAppConfiguration
public class PullRequestApprovalControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RemoteConfigurationServiceMock remoteConfigurationServiceMock;

    @Autowired
    private IssueCommentConfiguration configuration;

    @Autowired
    private AuthorizationConfiguration authorizationConfiguration;

    @Autowired
    private StatusConfiguration statusConfiguration;

    @Autowired
    private CommunicationServiceMock communicationServiceMock;

    private MockMvc mockMvc;

    private final Map<String, String> parameter = new HashMap<>(10);

    @Before
    public void setup() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        communicationServiceMock.setPostedStatus(null);
        communicationServiceMock.setPostedComment(null);

        System.out.println(authorizationConfiguration.getHttpHeaders());
    }

    @After
    public void cleanup() throws Exception {
        parameter.clear();
    }

    @Test
    public void issueEventComment_approved_should_send_success_when_auto_approval_is_authorized() throws Exception {

        System.out.println(System.getProperty("spring.profiles.active"));

        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", configuration.getApprovalCommentsList().get(0));
        parameter.put("last_state", Status.State.PENDING.getValue());
        parameter.put("user", "my_owner");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.ISSUE_COMMENT.getValue();

        // Simulate a calling of webservice
        MvcResult result = mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType)).andReturn();

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.SUCCESS.getValue());
        assertThat(status.getContext()).isEqualTo(statusConfiguration.getContextPullRequestApprovalStatus());

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void issueEventComment_approved_should_send_error_and_post_comment_when_auto_approval_is_forbidden() throws Exception {

        parameter.put("auto_approval.authorized", "false");
        parameter.put("issue_comment", configuration.getApprovalCommentsList().get(0));
        parameter.put("last_state", Status.State.PENDING.getValue());
        parameter.put("user", "my-owner");

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.ISSUE_COMMENT.getValue();

        // Simulate a calling of webservice
        MvcResult result = mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType)).andReturn();

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNull();

        Comment comment = communicationServiceMock.getPostedComment();
        assertThat(comment).isNotNull();
        assertThat(comment.getBody()).isNotEmpty();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.PRECONDITION_REQUIRED.value());
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
        MvcResult result = mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType)).andReturn();

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.SUCCESS.getValue());

        Comment comment = communicationServiceMock.getPostedComment();
        assertThat(comment).isNotNull();
        assertThat(comment.getBody()).isNotEmpty();
        assertThat(comment.getBody()).contains("because I was alone");
        assertThat(status.getContext()).isEqualTo(statusConfiguration.getContextPullRequestApprovalStatus());

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
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
        MvcResult result = mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType)).andReturn();

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.ERROR.getValue());
        assertThat(status.getContext()).isEqualTo(statusConfiguration.getContextPullRequestApprovalStatus());

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
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
        MvcResult result = mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType)).andReturn();

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(status.getContext()).isEqualTo(statusConfiguration.getContextPullRequestApprovalStatus());

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void pullRequestEvent_opened_should_send_pending() throws Exception {

        parameter.put("action", PullRequestEventAction.OPENED.getValue());

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("pull-request-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.PULL_REQUEST.getValue();

        // Simulate a calling of webservice
        MvcResult result = mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType)).andReturn();

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(status.getContext()).isEqualTo(statusConfiguration.getContextPullRequestApprovalStatus());

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void pullRequestEvent_synchronized_should_send_pending() throws Exception {

        parameter.put("action", PullRequestEventAction.SYNCHRONIZED.getValue());
        parameter.put("last_state", Status.State.SUCCESS.getValue());

        communicationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("pull-request-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.PULL_REQUEST.getValue();

        // Simulate a calling of webservice
        MvcResult result = mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType)).andReturn();

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.PENDING.getValue());
        assertThat(status.getContext()).isEqualTo(statusConfiguration.getContextPullRequestApprovalStatus());

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void pullRequestEvent_labeled_with_donotmerge_should_send_error() throws Exception {

        parameter.put("action", PullRequestEventAction.LABELED.getValue());
        parameter.put("last_state", Status.State.ERROR.getValue());
        parameter.put("label", "do not merge");

        communicationServiceMock.setParameters(parameter);
        remoteConfigurationServiceMock.setParameters(parameter);

        String content = FilesUtils.readFileInClasspath("pull-request-event-test.json", parameter);
        String eventType = EventHandlerDispatcher.Event.PULL_REQUEST.getValue();

        // Simulate a calling of webservice
        MvcResult result = mockMvc.perform(post("/webhook").
                content(content).
                header("x-github-event", eventType)).andReturn();

        // Assertions
        Status status = communicationServiceMock.getPostedStatus();
        assertThat(status).isNotNull();
        assertThat(status.getContext()).isEqualTo(statusConfiguration.getContextDoNotMergeLabelStatus());
        assertThat(status.getState()).isEqualTo(Status.State.ERROR.getValue());

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void asynchronous_request_should_be_processed_with_succes() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(4);

        Stream.of(
                getPullRequestTask(PullRequestEventAction.OPENED, Status.State.ERROR, HttpStatus.OK),
                getIssueCommentTask("approved", Status.State.PENDING, HttpStatus.PRECONDITION_REQUIRED),
                getPullRequestTask(PullRequestEventAction.OPENED, Status.State.SUCCESS, HttpStatus.OK),
                getPullRequestTask(PullRequestEventAction.SYNCHRONIZED, Status.State.PENDING, HttpStatus.OK),
                getIssueCommentTask("rejected", Status.State.ERROR, HttpStatus.OK),
                getPullRequestTask(PullRequestEventAction.SYNCHRONIZED, Status.State.PENDING, HttpStatus.OK)
        ).forEach(task -> executor.submit(task));

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }

    private Runnable getPullRequestTask(PullRequestEventAction action,
                                        Status.State latestState,
                                        HttpStatus expectedStatus) {

        Map<String, String> parameter = new HashMap<>(10);
        parameter.put("action", action.getValue());
        parameter.put("last_state", latestState.getValue());

        return () -> {
            try {
                String content = FilesUtils.readFileInClasspath("pull-request-event-test.json", parameter);
                String eventType = EventHandlerDispatcher.Event.PULL_REQUEST.getValue();

                HttpHeaders headers = new HttpHeaders();
                headers.set("x-github-event", eventType);

                MvcResult result = mockMvc.perform(post("/webhook").
                        content(content).
                        header("x-github-event", eventType)).andReturn();

                assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus.value());
            } catch (Exception e) {
                fail("Exception has occured", e);
            }
        };
    }

    private Runnable getIssueCommentTask(String comment,
                                         Status.State latestState,
                                         HttpStatus expectedStatus) {

        Map<String, String> parameter = new HashMap<>(10);
        parameter.put("auto_approval.authorized", "true");
        parameter.put("issue_comment", comment);
        parameter.put("last_state", latestState.getValue());
        parameter.put("user", "my_owner");

        return () -> {
            try {
                String content = FilesUtils.readFileInClasspath("issue-comment-event-test.json", parameter);
                String eventType = EventHandlerDispatcher.Event.ISSUE_COMMENT.getValue();

                HttpHeaders headers = new HttpHeaders();
                headers.set("x-github-event", eventType);

                MvcResult result = mockMvc.perform(post("/webhook").
                        content(content).
                        header("x-github-event", eventType)).andReturn();

                assertThat(result.getResponse().getStatus()).isEqualTo(expectedStatus.value());
            } catch (Exception e) {
                fail("Exception has occured", e);
            }
        };
    }
}
