package io.blackbird.aemconnector.core.utils;

public class PathUtils {

    public static final String PATH_SEPARATOR = "/";

    private PathUtils() {}

    public static String getName(String path) {
        if (path == null) {
            return null;
        }

        int lastSlash = path.lastIndexOf(PATH_SEPARATOR);

        return lastSlash < 0
                ? path
                : path.substring(lastSlash + 1);
    }

    public static String getParent(String path) {
        int lastSlash = path.lastIndexOf('/');
        return lastSlash > 0 ? path.substring(0, lastSlash) : null;
    }
}
