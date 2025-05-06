package io.blackbird.aemconnector.core.utils;

public class ObjectUtils {
    private ObjectUtils() {}

    public static <T, E extends Throwable> T ensureNotNull(T obj, ThrowingSupplier<? extends E> supplier) throws E {
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier must not be null");
        }

        if (obj == null) {
            throw supplier.get();
        }
        return obj;
    }
}
