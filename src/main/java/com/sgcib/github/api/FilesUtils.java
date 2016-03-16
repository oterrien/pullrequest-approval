package com.sgcib.github.api;

import org.apache.commons.lang3.text.StrSubstitutor;

import javax.validation.constraints.NotNull;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by oterrien092210 on 16/03/2016.
 */
public final class FilesUtils {

    private FilesUtils() {
    }

    public static String readFileInClasspath(@NotNull String fileName) throws Exception {
        return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(fileName).toURI())));
    }

    public static String readFileInClasspath(@NotNull String fileName, @NotNull Map<String, String> parameters) throws Exception {
        return StrSubstitutor.replace(readFileInClasspath(fileName), parameters);
    }
}
