package com.github;

import java.util.concurrent.atomic.AtomicInteger;

public class DiffKeyGenerator {
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    public static String next() {
        return "key" + SEQ.getAndIncrement();
    }

    public static void reset() {
        SEQ.set(1);
    }
}
