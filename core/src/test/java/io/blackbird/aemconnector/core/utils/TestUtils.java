package io.blackbird.aemconnector.core.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final public class TestUtils {
    private TestUtils() {
    }

    public static @NotNull String inputStreamToString(InputStream result) throws IOException {
        return new String(result.readAllBytes(), StandardCharsets.UTF_8);
    }
}
