package com.sgcib.github.api;

import com.sgcib.github.api.json.Comment;
import com.sgcib.github.api.json.Status;
import com.sgcib.github.api.service.ICommunicationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CommunicationServiceMock implements ICommunicationService {

    @Setter
    private Map<String, String> parameters;

    @Getter @Setter
    private Status postedStatus;

    @Getter @Setter
    private Comment postedComment;

    @Override
    public <T> T get(String url, Class<T> type) {
        try {
            return JsonUtils.parse(get(url), type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {

        try {
            if (url.contains("https://api.github.com/repos/my-owner/my-repository/pulls")) {
                return FilesUtils.readFileInClasspath("pull-request-test.json", parameters);
            }

            if (url.contains("https://api.github.com/repos/my-owner/my-repository/statuses")) {
                return FilesUtils.readFileInClasspath("statuses-test.json", parameters);
            }

            if (url.contains("https://api.github.com/repos/my-owner/my-repository/contents")) {
                return FilesUtils.readFileInClasspath("remote-config-files-test.json", parameters);
            }

            if (url.contains("https://raw.githubusercontent.com/my-owner/my-repository/my-branch")) {
                return FilesUtils.readFileInClasspath(".pullrequest-approval/configuration.properties", parameters);
            }

            if (url.contains("https://api.github.com/repos/my-owner/my-repository/collaborators")) {
                return FilesUtils.readFileInClasspath("collaborators-test.json", parameters);
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> HttpStatus post(String url, T object) {

        if (object instanceof Status) {
            this.postedStatus = (Status) object;
            return HttpStatus.OK;
        }

        if (object instanceof Comment) {
            this.postedComment = (Comment) object;
            return HttpStatus.OK;
        }

        return HttpStatus.NOT_IMPLEMENTED;
    }

}
