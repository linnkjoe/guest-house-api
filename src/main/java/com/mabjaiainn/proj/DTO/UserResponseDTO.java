package com.mabjaiainn.proj.DTO;

import com.mabjaiainn.proj.entity.Role;

public record UserResponseDTO(Long id, String username, String name, Role role, boolean enabled) {}
