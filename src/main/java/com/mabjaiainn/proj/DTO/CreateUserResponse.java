package com.mabjaiainn.proj.DTO;

import com.mabjaiainn.proj.entity.Role;

public record CreateUserResponse(
        Long id,
        String username,
        String name,
        Role role,
        boolean enabled,
        String temporaryPassword) {
}
