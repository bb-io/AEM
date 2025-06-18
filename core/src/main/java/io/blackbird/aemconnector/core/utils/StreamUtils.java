package io.blackbird.aemconnector.core.utils;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {

    private StreamUtils() {
    }

    public static <T> Stream<T> stream(Iterator<T> sourceIterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(sourceIterator, Spliterator.ORDERED),
                false);
    }
}
