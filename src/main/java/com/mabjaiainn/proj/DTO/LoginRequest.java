package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O utilizador e obrigatorio") String username,
        @NotBlank(message = "A palavra-passe e obrigatoria") String password
        ) {}
