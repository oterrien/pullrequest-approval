package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.CommunicationService;
import com.sgcib.github.api.eventhandler.Status;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MocksApplication.class)
@WebAppConfiguration
public class PullRequestApprovalControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CommunicationService communicationService;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void issueEventComment_approved_ShouldPostSuccessStatus() throws Exception {

        final Result result = new Result();

        // Mock communicationService
        {
            when(communicationService.get(anyString(), anyString(), any(Class.class))).
                    thenCallRealMethod();

            String pullsUrl = "https://api.github.com/repos/my-owner/my-repository/pulls";
            String pullsUrlResult = TestUtils.readFile("pull-request-test.json");
            when(communicationService.get(contains(pullsUrl), anyString())).
                    thenReturn(pullsUrlResult);

            String statusesUrl = "https://api.github.com/repos/my-owner/my-repository/statuses";
            String statusesUrlResult = TestUtils.readFile("statuses-test.json");
            when(communicationService.get(Mockito.contains(statusesUrl), anyString())).
                    thenReturn(statusesUrlResult);

            String contentsUrl = "https://api.github.com/repos/my-owner/my-repository/contents";
            String contentsUrlResult = TestUtils.readFile("remote-config-files-test.json");
            when(communicationService.get(Mockito.contains(contentsUrl), anyString())).
                    thenReturn(contentsUrlResult);

            String downloadUrl = "https://raw.githubusercontent.com/my-owner/my-repository/my-branch";
            String downloadUrlResult = TestUtils.readFile("configuration-test.properties");
            when(communicationService.get(contains(downloadUrl), anyString())).
                    thenReturn(downloadUrlResult);

            when(communicationService.post(contains(statusesUrl), any(Status.class), anyString())).
                    then(invocationOnMock -> {
                        result.setStatus(invocationOnMock.getArgumentAt(1, Status.class));
                        return HttpStatus.OK;
                    });
        }

        String content = TestUtils.readFile("issue-comment-event-test.json");
        String eventType = "issue_comment";

        // Simulate a calling of webservice
        mockMvc.perform(MockMvcRequestBuilders.post("/webhook").
                content(content).
                header("x-github-event", eventType));

        // Assertions
        Status status = result.getStatus();
        assertThat(status).isNotNull();
        assertThat(status.getState()).isEqualTo(Status.State.SUCCESS.getState());
    }

    @Data
    public class Result {

        private Status status;
    }
}
