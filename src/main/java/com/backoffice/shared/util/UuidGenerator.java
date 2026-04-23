package com.backoffice.shared.util;

import java.util.UUID;

/**
 * UUID v7 generator — time-ordered UUIDs per RFC 9562.
 * Uses current timestamp in the most-significant bits for natural ordering.
 */
public final class UuidGenerator {

    private UuidGenerator() {}

    public static UUID newUuid7() {
        long timestamp = System.currentTimeMillis();
        long msb = (timestamp << 16) & 0xFFFFFFFFFFFF0000L;
        msb |= 0x7000L; // version 7
        msb |= (long)(Math.random() * 0x0FFF);

        long lsb = (long)(Math.random() * Long.MAX_VALUE);
        lsb = (lsb & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L; // variant 10

        return new UUID(msb, lsb);
    }
}
