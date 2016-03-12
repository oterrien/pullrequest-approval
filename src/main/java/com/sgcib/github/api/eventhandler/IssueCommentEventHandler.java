package com.sgcib.github.api.eventhandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sgcib.github.api.payloayd.IssueCommentPayload;
import com.sgcib.github.api.payloayd.PullRequest;
import com.sgcib.github.api.payloayd.Status;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Olivier on 07/03/2016.
 */
@Component
public class IssueCommentEventHandler extends AdtEventHandler<IssueCommentPayload> implements IEventHandler {

    private List<String> acceptedComments = Stream.of("approved", "ok").
            collect(Collectors.toList());

    private List<String> refusedComments = Stream.of("refused", "ko", "rejected").
            collect(Collectors.toList());

    public IssueCommentEventHandler() {
        super(IssueCommentPayload.class);
    }

    @Override
    public void handle(IssueCommentPayload event) throws IOException {

        String comment = event.getComment().getBody().trim().toLowerCase();

        if (acceptedComments.contains(comment)) {

            String pullUrl = event.getIssue().getPullRequest().getUrl();

            PullRequest pullRequest = getPullRequest(pullUrl);

            if (pullRequest == null)
                return;

            Status status = new Status();
            status.setContext("manual/pullrequest-approval");
            status.setDescription("The PullRequest has been approved");
            status.setState("success");
            status.setTargetUrl("");

            String statusesUrl = pullRequest.getStatusesUrl();

            logger.info(statusesUrl);


            postStatus(statusesUrl, status);

            return;
        }

        if (refusedComments.contains(comment)) {

            String pullUrl = event.getIssue().getPullRequest().getUrl();

            PullRequest pullRequest = getPullRequest(pullUrl);
            if (pullRequest == null)
                return;

            Status status = new Status();
            status.setContext("manual/pullrequest-approval");
            status.setDescription("The PullRequest has been rejected");
            status.setState("failure");
            status.setTargetUrl("");

            String statusesUrl = pullRequest.getStatusesUrl();
            postStatus(statusesUrl, status);

            return;
        }
    }

    private PullRequest getPullRequest(String url) throws IOException {

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(url, String.class);
        return jsonService.parse(PullRequest.class, result);
    }


    private void postStatus(String url, Status status) throws JsonProcessingException {

        HttpHeaders headers = new HttpHeaders();
        String auth = "oterrien@neuf.fr" + ":" + "xxxxxxx";
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();

        logger.info(jsonService.serialize(status));
        logger.info(headers.toString());

        restTemplate.postForObject(url, new HttpEntity<>(jsonService.serialize(status), headers), String.class);

    }
}
