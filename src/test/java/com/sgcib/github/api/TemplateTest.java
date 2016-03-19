package com.sgcib.github.api;

import com.sgcib.github.api.json.Permissions;
import com.sgcib.github.api.json.User;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateTest {

    @Test
    public void advice_message_should_be_fulfilled() throws Exception {

        String expected = FilesUtils.readFileInClasspath("message-advice-result-test.md");

        String user = "userTest";
        List<String> autoApprovedList = Stream.of("auto-approved", "auto-validated").collect(Collectors.toList());

        Map<String, String> param = new HashMap<>(10);
        param.put("user", "@" + user);
        param.put("issue.comments.list.auto_approval", autoApprovedList.stream().collect(Collectors.joining(" or ")));
        String actual =  FilesUtils.readFileInClasspath("message-advice.md", param);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void alert_message_should_be_fulfilled() throws Exception {

        String expected = FilesUtils.readFileInClasspath("message-alert-result-test.md");

        Stream.Builder<User> builder = Stream.builder();
        builder.add(createUser("owner1", true));
        builder.add(createUser("owner2", true));
        builder.add(createUser("contributor1", false));

        String user = "userTest";
        List<String> autoApprovedList = Stream.of("auto-approved", "auto-validated").collect(Collectors.toList());
        List<User> administrators = builder.build().filter(u -> u.getPermissions().isAdmin()).collect(Collectors.toList());
        String reason = "I was alone";

        Map<String, String> param = new HashMap<>(10);
        param.put("user", user);
        param.put("owners", administrators.stream().map(u -> "@" + u.getLogin()).collect(Collectors.joining(", ")));
        param.put("issue.comments.list.auto_approval", autoApprovedList.stream().collect(Collectors.joining(" or ")));
        param.put("reason", reason);
        String actual =  FilesUtils.readFileInClasspath("message-alert.md", param);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    private static User createUser(String login, boolean isAdmin){

        Permissions permissions = new Permissions();
        permissions.setAdmin(isAdmin);

        User user = new User();
        user.setLogin(login);
        user.setPermissions(permissions);

        return user;

    }
}
