package com.sgcib.github.api;

import com.sgcib.github.api.payloayd.PullRequest;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class JsonServiceTest {

    @Test
    public void test_pull_request_parsing()throws Exception {
        PullRequest pullRequest = new JsonService().parse(TestUtils.readFile("pull-request-test.json"), PullRequest.class);

        Assertions.assertThat(pullRequest).isNotNull();
        Assertions.assertThat(pullRequest.getUrl()).contains("https://api.github.com/repos/my-owner/my-repository/pulls");
        Assertions.assertThat(pullRequest.getStatusesUrl()).contains("https://api.github.com/repos/my-owner/my-repository/statuses");
        Assertions.assertThat(pullRequest.getUser()).isNotNull();
    }


}
