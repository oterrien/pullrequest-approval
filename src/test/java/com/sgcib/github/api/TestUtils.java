package com.sgcib.github.api;

import org.apache.commons.lang3.text.StrSubstitutor;

import javax.validation.constraints.NotNull;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public final class TestUtils {

    private TestUtils() {

    }

    public static String readFile(@NotNull String fileName) throws Exception {
        return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(fileName).toURI())));
    }

    public static String readFile(@NotNull String fileName, @NotNull Map<String, String> parameters) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(fileName).toURI())));
        return StrSubstitutor.replace(content, parameters);
    }
}
