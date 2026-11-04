package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GoogleOAuthCompleteRequest(
        String code,
        @Size(max = 9, message = "O telefone deve ter 9 algarismos")
        @Pattern(regexp = "^$|^[0-9]{9}$", message = "O telefone deve ter 9 algarismos")
        String phoneNumber
) {
}
