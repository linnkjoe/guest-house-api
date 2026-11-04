package com.mabjaiainn.proj.DTO;

import com.mabjaiainn.proj.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Criacao de contas da equipa (ADMIN ou GESTOR). Contas CLIENTE nascem em POST /clients. */
public record CreateUserRequest(
        @NotBlank(message = "O utilizador e obrigatorio")
        @Size(min = 3, max = 100, message = "O utilizador deve ter entre 3 e 100 caracteres") String username,
        @Size(min = 8, max = 72, message = "A palavra-passe deve ter entre 8 e 72 caracteres") String password,
        @Size(max = 72, message = "A confirmacao nao pode ter mais de 72 caracteres") String confirmPassword,
        @Size(max = 100, message = "O nome nao pode ter mais de 100 caracteres") String name,
        @NotNull(message = "O perfil e obrigatorio") Role role
        ) {}
