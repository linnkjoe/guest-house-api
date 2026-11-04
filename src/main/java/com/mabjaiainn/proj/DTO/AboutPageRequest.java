package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AboutPageRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 240) String introduction,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 200) String address,
        @NotBlank
        @Pattern(regexp = "[0-9+() .-]{7,30}", message = "Indique um contacto telefonico valido")
        String contact
) {
}
