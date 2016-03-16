package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.ICommunicationService;
import com.sgcib.github.api.eventhandler.Status;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CommunicationServiceMock implements ICommunicationService {

    @Getter @Setter
    private Map<String, String> parameters;

    @Getter
    private Status status;

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
                return FilesUtils.readFileInClasspath("configuration-test.properties", parameters);
            }

            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> HttpStatus post(String url, T object) {

        if (object instanceof Status) {
            this.status = (Status) object;
            return HttpStatus.OK;
        }
        return HttpStatus.NOT_IMPLEMENTED;
    }

}
