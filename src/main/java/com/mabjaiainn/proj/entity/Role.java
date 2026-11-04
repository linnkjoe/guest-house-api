package com.mabjaiainn.proj.entity;

/**
 * Perfis da aplicação. No Spring Security aparecem como ROLE_ADMIN,
 * ROLE_GESTOR e ROLE_CLIENTE (hasRole('ADMIN') etc.).
 */
public enum Role {
    ADMIN, GESTOR, CLIENTE
}
