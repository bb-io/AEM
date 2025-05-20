package io.blackbird.aemconnector.core.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MemoizingSupplierTest {

    @Test
    void testMemoization() {
        AtomicInteger counter = new AtomicInteger(0);
        Supplier<Integer> originalSupplier = () -> counter.incrementAndGet();
        Supplier<Integer> memoizingSupplier = MemoizingSupplier.memoizeWithExpiration(
                originalSupplier, 100, TimeUnit.MILLISECONDS);

        int firstValue = memoizingSupplier.get();
        assertEquals(1, firstValue);
        assertEquals(1, counter.get());

        int secondValue = memoizingSupplier.get();
        assertEquals(1, secondValue);
        assertEquals(1, counter.get());

        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        int thirdValue = memoizingSupplier.get();
        assertEquals(2, thirdValue);
        assertEquals(2, counter.get());

        int fourthValue = memoizingSupplier.get();
        assertEquals(2, fourthValue);
        assertEquals(2, counter.get());
    }

}
