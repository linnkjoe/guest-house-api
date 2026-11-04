package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.*;
import java.util.List;

public record CustomPackRequest(
        @NotBlank(message = "De um nome a esta estadia")
        @Size(min = 2, max = 60, message = "O nome deve ter entre 2 e 60 caracteres") String name,
        @NotNull(message = "A duracao e obrigatoria")
        @Min(value = 1, message = "A duracao minima e uma hora") Integer duration,
        List<Long> featureIds
        ) {}
