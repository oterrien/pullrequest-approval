package com.sgcib.github.api;

import com.sgcib.github.api.eventhandler.JsonService;
import com.sgcib.github.api.payloayd.PullRequest;
import com.sgcib.github.api.payloayd.PullRequestPayload;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JSonParserTest {

    @Test
    public void testReplaceUrl() {

        String key = "{/number}";
        RestTemplate restTemplate = new RestTemplate();
        PullRequest result = restTemplate.getForObject("https://api.github.com/repos/baxterthehacker/public-repo/pulls{/number}".replace(key, "/1"), PullRequest.class);
        System.out.println(result);

        Map<String, String> param = new HashMap<String, String>();
        param.put("number", "1");
        result = restTemplate.getForObject("https://api.github.com/repos/baxterthehacker/public-repo/pulls{/number}".replace("{/", "/{"), PullRequest.class, param);
        System.out.println(result);
    }

    @Test
    public void testParsing() {


        try {
            String content = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("pull_request_event.json").toURI())));

            System.out.println(content);

            PullRequestPayload obj = new JsonService().parse(content, PullRequestPayload.class);

            Assert.assertEquals("opened", obj.getAction());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            Assert.fail("Exception");
        }
    }

    @Test
    public void testLinkedHashMap() {

        System.out.println(Boolean.parseBoolean("true"));

        final int MAX_ENTRIES = 5;

        LinkedHashMap lhm = new LinkedHashMap(MAX_ENTRIES + 1, .75F, false) {

            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_ENTRIES;
            }
        };
        lhm.put(0, "H");
        lhm.put(1, "E");
        lhm.put(2, "L");
        lhm.put(3, "L");
        lhm.put(4, "O");

        System.out.println("" + lhm);

        lhm.put(5, "T");

        System.out.println("" + lhm);
    }

}
