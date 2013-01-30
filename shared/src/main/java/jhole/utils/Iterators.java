package jhole.utils;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class Iterators {
    public static <T> Iterable<T> iterateOnce(final Iterator<T> iterator) {
        final AtomicBoolean used = new AtomicBoolean();
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                if (!used.compareAndSet(false, true)) {
                    throw new IllegalStateException("this iterable was already iterated");
                }
                return iterator;
            }
        };
    }
}
