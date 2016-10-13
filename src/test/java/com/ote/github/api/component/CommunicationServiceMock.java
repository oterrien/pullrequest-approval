package com.ote.github.api.component;

import com.ote.github.api.FilesUtils;
import com.ote.github.api.JsonUtils;
import com.ote.github.api.component.ICommunicationService;
import com.ote.github.api.json.Comment;
import com.ote.github.api.json.Status;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CommunicationServiceMock implements ICommunicationService {

    @Setter
    private Map<String, String> parameters;

    @Getter
    private List<Status> postedStatuses = new ArrayList<>(10);

    @Getter
    private List<Comment> postedComments = new ArrayList<>(10);

    public void clean() {
        postedStatuses.clear();
        postedComments.clear();
    }

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

            if (url.contains("https://api.github.com/repos/my-owner/my-repository/issues")) {
                return FilesUtils.readFileInClasspath("issue-test.json", parameters);
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> HttpStatus post(String url, T object) {

        if (object instanceof Status) {
            this.postedStatuses.add((Status) object);
            return HttpStatus.OK;
        }

        if (object instanceof Comment) {
            this.postedComments.add((Comment) object);
            return HttpStatus.OK;
        }

        return HttpStatus.NOT_IMPLEMENTED;
    }

}
