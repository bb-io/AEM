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

    public static String stripParent(String fullPath, String parentPath) {
        if (fullPath == null || parentPath == null) {
            return fullPath;
        }

        String normalizedParent = parentPath.endsWith(PATH_SEPARATOR)
                ? parentPath.substring(0, parentPath.length() - 1)
                : parentPath;

        String prefix = normalizedParent + PATH_SEPARATOR;

        if (fullPath.startsWith(prefix)) {
            return fullPath.substring(prefix.length());
        }

        return fullPath;
    }
}
