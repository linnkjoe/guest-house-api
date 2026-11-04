package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.*;

/** Auto cadastro de um cliente que chega pela primeira vez. */
public record RegisterRequest(
        @NotBlank(message = "O primeiro nome e obrigatorio")
        @Size(min = 2, max = 50, message = "O primeiro nome deve ter entre 2 e 50 caracteres") String clientFirstName,
        @NotBlank(message = "O apelido e obrigatorio")
        @Size(min = 2, max = 50, message = "O apelido deve ter entre 2 e 50 caracteres") String clientLastName,
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Insira um email valido")
        @Size(max = 100, message = "O email nao pode ter mais de 100 caracteres") String clientEmail,
        @NotNull(message = "O numero e obrigatorio")
        @Min(value = 100000000, message = "O numero deve ter pelo menos 9 algarismos")
        @Max(value = 999999999, message = "O numero deve ter no maximo 9 algarismos") Integer phoneNumber,
        @NotBlank(message = "A palavra-passe e obrigatoria")
        @Size(min = 8, max = 72, message = "A palavra-passe deve ter entre 8 e 72 caracteres") String clientPassword,
        @NotBlank(message = "A confirmacao da palavra-passe e obrigatoria")
        @Size(max = 72, message = "A confirmacao nao pode ter mais de 72 caracteres") String confirmClientPassword
        ) {}
