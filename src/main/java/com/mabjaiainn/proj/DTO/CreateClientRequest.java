package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateClientRequest(
        @NotNull @Min(100000000) @Max(999999999) Integer phoneNumber,
        @NotBlank @Size(min = 2, max = 50) String clientFirstName,
        @NotBlank @Size(min = 2, max = 50) String clientLastName,
        @NotBlank @Email @Size(max = 100) String clientEmail) {
}
