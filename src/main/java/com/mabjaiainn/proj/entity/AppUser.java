package com.mabjaiainn.proj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Conta de acesso à API (admin, gestor ou cliente).
 * Contas CLIENTE ficam ligadas ao registo Client correspondente.
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_seq")
    @SequenceGenerator(name = "app_user_seq", sequenceName = "APP_USER_SEQ", allocationSize = 1)
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private Role role;

    @Column(name = "account_enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @OneToOne
    @JoinColumn(name = "client_id", unique = true)
    private Client client;

    public AppUser() {
    }

    public AppUser(String username, String passwordHash, String displayName, Role role, Client client) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        this.client = client;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
