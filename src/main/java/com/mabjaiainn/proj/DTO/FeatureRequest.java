package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record FeatureRequest(
        @NotBlank(message = "O nome do extra e obrigatorio")
        @Size(min = 2, max = 80, message = "O nome do extra deve ter entre 2 e 80 caracteres") String name,
        @Size(max = 200, message = "A descricao nao pode ter mais de 200 caracteres") String description,
        @NotNull(message = "O preco do extra e obrigatorio")
        @DecimalMin(value = "0.00", message = "O preco nao pode ser negativo") BigDecimal price,
        Boolean active
        ) {}
