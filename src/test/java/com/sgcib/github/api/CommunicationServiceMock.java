package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.EventHandlerException;
import com.sgcib.github.api.eventhandler.ICommunicationService;
import com.sgcib.github.api.eventhandler.Status;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

@Service
public class CommunicationServiceMock implements ICommunicationService {

    private Map<String, String> parameters;

    public CommunicationServiceMock() {
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public <T> T get(String url, Class<T> type) throws EventHandlerException {
        String result = get(url);
        try {
            return JsonUtils.parse(result, type);
        } catch (IOException e) {
            throw new EventHandlerException(e, HttpStatus.UNPROCESSABLE_ENTITY, "Error while parsing result from " + url, result);
        }
    }

    @Override
    public String get(@NotNull String url) {

        try {
            if (url.contains("https://api.github.com/repos/my-owner/my-repository/pulls")) {
                return TestUtils.readFile("pull-request-test.json", parameters);
            }

            if (url.contains("https://api.github.com/repos/my-owner/my-repository/statuses")) {
                return TestUtils.readFile("statuses-test.json", parameters);
            }

            if (url.contains("https://api.github.com/repos/my-owner/my-repository/contents")) {
                return TestUtils.readFile("remote-config-files-test.json", parameters);
            }

            if (url.contains("https://raw.githubusercontent.com/my-owner/my-repository/my-branch")) {
                return TestUtils.readFile("configuration-test.properties", parameters);
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

    @Getter
    private Status status;

}
