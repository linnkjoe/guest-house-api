package com.mabjaiainn.proj.service;

import java.security.SecureRandom;
import java.util.Base64;

final class TemporaryPasswordGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TemporaryPasswordGenerator() {
    }

    static String generate() {
        byte[] randomBytes = new byte[18];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
