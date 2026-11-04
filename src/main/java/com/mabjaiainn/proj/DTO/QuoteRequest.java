package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.*;
import java.util.List;

/** Escolhas do cliente para calcular o preco de uma estadia personalizada. */
public record QuoteRequest(
        @NotNull(message = "A duracao e obrigatoria")
        @Min(value = 1, message = "A duracao minima e uma hora") Integer duration,
        List<Long> featureIds
        ) {}
