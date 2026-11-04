package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.NotNull;

public record SetUserEnabledRequest(
        @NotNull(message = "Indique se a conta deve estar ativa")
        Boolean enabled
) {
}
