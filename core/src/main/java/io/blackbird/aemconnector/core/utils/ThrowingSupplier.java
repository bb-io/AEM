package io.blackbird.aemconnector.core.utils;

@FunctionalInterface
public interface ThrowingSupplier<E extends Throwable> {
    E get() throws E;
}
