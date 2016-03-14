package com.sgcib.github.api;

import org.apache.commons.lang3.text.StrSubstitutor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by Olivier on 14/03/2016.
 */
public final class TestUtils {

    public static String readFile(String fileName) throws Exception {
        return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(fileName).toURI())));
    }

    public static String readFile(String fileName, Map<String, String> parameters) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(fileName).toURI())));
        return StrSubstitutor.replace(content, parameters);
    }
}
