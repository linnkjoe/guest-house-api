package com.mabjaiainn.proj.DTO;

import com.mabjaiainn.proj.entity.Role;

public record AuthUserDetails(
        String username,
        String passwordHash,
        String displayName,
        Role role,
        Long clientId,
        boolean enabled,
        boolean mustChangePassword
) {
}
