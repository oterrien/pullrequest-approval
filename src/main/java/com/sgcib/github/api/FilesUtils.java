package com.sgcib.github.api;

import org.apache.commons.lang3.text.StrSubstitutor;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by oterrien092210 on 16/03/2016.
 */
public final class FilesUtils {

    private FilesUtils() {
    }

    public static String readFileInClasspath(@NotNull String fileName) throws URISyntaxException, IOException {
        return new Scanner(ClassLoader.getSystemResourceAsStream(fileName), StandardCharsets.UTF_8.name()).
                useDelimiter("\\Z").
                next();
    }

    public static String readFileInClasspath(@NotNull String fileName, @NotNull Map<String, String> parameters) throws URISyntaxException, IOException {
        return StrSubstitutor.replace(readFileInClasspath(fileName), parameters);
    }
}
