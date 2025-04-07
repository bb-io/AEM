package io.blackbird.aemconnector.core.utils;

import org.apache.johnzon.core.JsonPointerUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilsTest {

    @Test
    void shouldGetPathName() {
        String path = "/content/wknd/language-masters/en/adventures/whistler-mountain-biking";
        String actualName = PathUtils.getName(path);
        String expectedName = "whistler-mountain-biking";

        assertEquals(expectedName, actualName);
    }

    @Test
    void shouldBeNullWhenPathIsNull() {
        assertNull(PathUtils.getName(null));
    }

    @Test
    void shouldBeAsIsWhenPathNotContainSlashes() {
        String path = "content";
        assertEquals(path, PathUtils.getName(path));
    }

    @Test
    void shouldReturnPathParent() {
        String path = "/content/wknd/language-masters/en/adventures/whistler-mountain-biking";
        String actual = PathUtils.getParent(path);
        assertEquals("/content/wknd/language-masters/en/adventures", actual);
    }
}