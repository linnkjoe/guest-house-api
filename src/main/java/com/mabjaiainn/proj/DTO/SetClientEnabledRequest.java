package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.NotNull;

public record SetClientEnabledRequest(@NotNull Boolean enabled) {
}
