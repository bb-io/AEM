package io.blackbird.aemconnector.core.utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A supplier that memoizes the result of another supplier for a specified duration.
 * This is a replacement for the deprecated Guava Suppliers.memoizeWithExpiration.
 *
 * @param <T> the type of results supplied by this supplier
 */
public class MemoizingSupplier<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    private final long expirationNanos;

    private volatile T value;
    private volatile long expirationTimeNanos;

    private MemoizingSupplier(Supplier<T> delegate, long duration, TimeUnit unit) {
        this.delegate = Objects.requireNonNull(delegate);
        this.expirationNanos = unit.toNanos(duration);
        this.expirationTimeNanos = 0;
    }

    /**
     * Creates a supplier that caches the result of the given supplier for the specified duration.
     *
     * @param delegate the supplier whose result should be cached
     * @param duration the duration for which the result should be cached
     * @param unit     the time unit of the duration
     * @param <T>      the type of results supplied by this supplier
     * @return a supplier that caches the result of the given supplier for the specified duration
     */
    public static <T> Supplier<T> memoizeWithExpiration(Supplier<T> delegate, long duration, TimeUnit unit) {
        return new MemoizingSupplier<>(delegate, duration, unit);
    }

    @Override
    public T get() {
        long now = System.nanoTime();
        if (expirationTimeNanos == 0 || now - expirationTimeNanos >= 0) {
            synchronized (this) {
                if (expirationTimeNanos == 0 || now - expirationTimeNanos >= 0) {
                    T t = delegate.get();
                    value = t;
                    expirationTimeNanos = now + expirationNanos;
                    return t;
                }
            }
        }
        return value;
    }

}
