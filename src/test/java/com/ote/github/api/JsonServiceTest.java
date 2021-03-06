package com.ote.github.api;

import com.ote.github.api.FilesUtils;
import com.ote.github.api.JsonUtils;
import com.ote.github.api.json.PullRequest;
import com.ote.github.api.json.Status;
import com.ote.github.api.json.Statuses;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JsonServiceTest {

    @Test
    public void test_pull_request_parsing() throws Exception {

        PullRequest pullRequest = JsonUtils.parse(FilesUtils.readFileInClasspath("pull-request-test.json"), PullRequest.class);

        Assertions.assertThat(pullRequest).isNotNull();
        Assertions.assertThat(pullRequest.getUrl()).contains("https://api.github.com/repos/my-owner/my-repository/pulls");
        Assertions.assertThat(pullRequest.getStatusesUrl()).contains("https://api.github.com/repos/my-owner/my-repository/statuses");
        Assertions.assertThat(pullRequest.getUser()).isNotNull();
    }

    @Test
    public void test_status_parsing() throws Exception {

        final Map<String, String> parameters = new HashMap<>(10);
        parameters.put("last_state", "pending");

        String str = "{\"statuses\":" + FilesUtils.readFileInClasspath("statuses-test.json", parameters) + "}";
        Statuses statuses = JsonUtils.parse(str, Statuses.class);

        Assertions.assertThat(statuses).isNotNull();
        Optional<Status> status = statuses.getStatuses().stream().findFirst();
        Assertions.assertThat(status.isPresent()).isTrue();
        Assertions.assertThat(status.get().getState()).isEqualTo("pending");
        Assertions.assertThat(status.get().getContext()).isEqualTo("manual/pullrequest-approval");

        System.out.println(JsonUtils.serialize(status.get()));
    }
}
