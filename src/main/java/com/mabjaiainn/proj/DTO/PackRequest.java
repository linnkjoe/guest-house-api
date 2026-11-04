package com.mabjaiainn.proj.DTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/** Dados para criar ou atualizar uma estadia do catalogo. */
public record PackRequest(
        @NotBlank(message = "O nome da estadia e obrigatorio")
        @Size(min = 2, max = 60, message = "O nome deve ter entre 2 e 60 caracteres") String packName,
        @Size(max = 500, message = "A descricao nao pode ter mais de 500 caracteres") String description,
        @NotNull(message = "A duracao e obrigatoria")
        @Min(value = 1, message = "Pacotes tem duracao minima duma hora")
        @Max(value = 720, message = "A duracao maxima e 720 horas") Integer duration,
        @NotNull(message = "O preco do pacote e obrigatorio")
        @DecimalMin(value = "0.00", message = "O preco nao pode ser negativo") BigDecimal packPrice,
        Boolean packAc,
        List<Long> featureIds,
        @DecimalMin(value = "0.00", message = "O desconto nao pode ser negativo")
        @DecimalMax(value = "90.00", message = "O desconto maximo e 90 por cento")
        BigDecimal promotionDiscount
        ) {
    public PackRequest(String packName, String description, Integer duration, BigDecimal packPrice,
            Boolean packAc, List<Long> featureIds) {
        this(packName, description, duration, packPrice, packAc, featureIds, BigDecimal.ZERO);
    }
}
