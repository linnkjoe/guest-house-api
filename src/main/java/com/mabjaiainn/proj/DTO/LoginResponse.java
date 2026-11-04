package com.mabjaiainn.proj.DTO;

/**
 * role: ADMIN, GESTOR ou CLIENTE. clientId so vem preenchido para CLIENTE.
 * expiresIn: validade do token em segundos.
 */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        String username,
        String name,
        String role,
        Long clientId,
        boolean mustChangePassword
        ) {}
