package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmailVerificationRequest(
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "O código deve conter 6 algarismos") String code
) {
}
