package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactReplyRequest(
        @NotBlank(message = "Escreva a resposta")
        @Size(max = 2000, message = "A resposta nao pode exceder 2000 caracteres")
        String reply
) {
}
