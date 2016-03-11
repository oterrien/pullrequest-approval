package test.com.sgcib.github.api;

import com.sgcib.github.api.JSOnParser;
import org.eclipse.egit.github.core.event.PullRequestPayload;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by Olivier on 11/03/2016.
 */
public class JSonParserTest {

    @Test
    public void test() {

        try {
            String content = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("pull request event.json").toURI())));

            System.out.println(content);

            PullRequestPayload obj = new JSOnParser().parser(PullRequestPayload.class, content);

            System.out.println(obj.getAction());
        }
        catch (IOException |URISyntaxException e){
            e.printStackTrace();
            Assert.fail("Exception");
        }

    }
}
