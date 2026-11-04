package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "A palavra-passe atual e obrigatoria")
        @Size(max = 72, message = "A palavra-passe atual nao pode exceder 72 caracteres")
        String currentPassword,
        @NotBlank(message = "A nova palavra-passe e obrigatoria")
        @Size(min = 8, max = 72, message = "A nova palavra-passe deve ter entre 8 e 72 caracteres")
        String newPassword
) {
}
