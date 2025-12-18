package io.blackbird.aemconnector.core.utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final public class TestUtils {
    private TestUtils() {
    }

    public static @NotNull String inputStreamToString(InputStream result) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096]; // 4 KB – good default size
        int read;
        while ((read = result.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
}
