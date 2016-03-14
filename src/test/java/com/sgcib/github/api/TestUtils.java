package com.sgcib.github.api;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Olivier on 14/03/2016.
 */
public final class TestUtils {

    public static String readFile(String fileName) throws Exception {
        return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(fileName).toURI()))).replaceAll("\r\n", "");
    }
}
