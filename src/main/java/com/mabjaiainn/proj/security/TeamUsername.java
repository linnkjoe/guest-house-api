package com.mabjaiainn.proj.security;

import java.util.Locale;

public final class TeamUsername {

    private static final String SUFFIX = "@mbj.inn";

    private TeamUsername() {
    }

    public static boolean isValid(String username) {
        return username != null && username.matches("(?i)^[^@\\s]+@mbj\\.inn$");
    }

    public static String normalizeBootstrapUsername(String configuredUsername) {
        if (configuredUsername == null || configuredUsername.isBlank()) {
            throw new IllegalStateException("Bootstrap admin username must not be blank");
        }
        String username = configuredUsername.trim().toLowerCase(Locale.ROOT);
        if (!username.contains("@")) {
            username += SUFFIX;
        }
        if (!isValid(username) || username.length() > 100) {
            throw new IllegalStateException("Bootstrap admin username must end with @mbj.inn and contain at most 100 characters");
        }
        return username;
    }
}
