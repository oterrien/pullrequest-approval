package test.com.sgcib.github.api;

import com.sgcib.github.api.JSOnParser;
import com.sgcib.github.api.payloayd.PullRequest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by Olivier on 11/03/2016.
 */
public class JSonParserTest {

    @Test
    public void testReplaceUrl() {

        String key = "{/number}";
        RestTemplate restTemplate = new RestTemplate();
        PullRequest result = restTemplate.getForObject("https://api.github.com/repos/baxterthehacker/public-repo/pulls{/number}".replace(key, "/1"), PullRequest.class);
        System.out.println(result);

        Map<String, String> param = new HashMap<String, String>();
        param.put("number", "1");
        result = restTemplate.getForObject("https://api.github.com/repos/baxterthehacker/public-repo/pulls{/number}".replace("{/", "/{"), PullRequest.class,param);
        System.out.println(result);
    }

    @Test
    public void testParsing() {


        try {
            String content = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("pull request event.json").toURI())));

            System.out.println(content);

            PullRequest obj = new JSOnParser().parse(PullRequest.class, content);

            Assert.assertEquals("opened", obj.getAction());
        }
        catch (IOException |URISyntaxException e){
            e.printStackTrace();
            Assert.fail("Exception");
        }

    }
}
