package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactMessageRequest(
        @NotBlank(message = "Indique o assunto da mensagem")
        @Size(max = 120, message = "O assunto nao pode exceder 120 caracteres")
        String subject,
        @NotBlank(message = "Escreva a sua mensagem")
        @Size(max = 2000, message = "A mensagem nao pode exceder 2000 caracteres")
        String message
) {
}
