package com.sgcib.github.api.eventhandler;

import com.sgcib.github.api.payloayd.IssueComment;
import com.sgcib.github.api.payloayd.PullRequest;
import com.sgcib.github.api.payloayd.Status;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class IssueCommentEventHandler extends AdtEventHandler<IssueComment> implements IEventHandler {

    private List<String> acceptedComments = Stream.of("approved", "ok").
            collect(Collectors.toList());

    private List<String> refusedComments = Stream.of("refused", "ko", "rejected").
            collect(Collectors.toList());

    public IssueCommentEventHandler() {
        super(IssueComment.class);
    }

    @Override
    public void handle(IssueComment event) {

        String comment = event.getComment().getBody().trim().toLowerCase();

        if (acceptedComments.contains(comment)) {

            String pullUrl = event.getRepository().getPullsUrl().replace("{/", "/{");
            int number = event.getIssue().getNumber();
            PullRequest pullRequest = getPullRequest(pullUrl, number);

            if (pullRequest == null)
                return;

            Status status = new Status();
            status.setContext("manual/pullrequest-approval");
            status.setDescription("The PullRequest has been approved");
            status.setStatus("success");

            String statusesUrl = pullRequest.getStatusesUrl();
            postStatus(statusesUrl, status);

            return;
        }

        if (refusedComments.contains(comment)) {

            String pullUrl = event.getRepository().getPullsUrl().replace("{/", "/{");
            int number = event.getIssue().getNumber();
            PullRequest pullRequest = getPullRequest(pullUrl, number);
            if (pullRequest == null)
                return;

            Status status = new Status();
            status.setContext("manual/pullrequest-approval");
            status.setDescription("The PullRequest has been rejected");
            status.setStatus("failure");

            String statusesUrl = pullRequest.getStatusesUrl();
            postStatus(statusesUrl, status);

            return;
        }

    }

    private PullRequest getPullRequest(String url, int number) {

        Map<String, String> param = new HashMap<>(1);
        param.put("number", Integer.toString(number));

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, PullRequest.class);
    }

    private void postStatus(String url, Status status) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(url, status, String.class);

    }

}
