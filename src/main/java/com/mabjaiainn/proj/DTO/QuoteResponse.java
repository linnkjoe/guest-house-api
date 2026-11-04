package com.mabjaiainn.proj.DTO;

import java.math.BigDecimal;
import java.util.List;

/** Preco calculado, com o detalhe de como se chegou a ele. */
public record QuoteResponse(
        Long basePackId,
        String basePackName,
        Integer baseDuration,
        BigDecimal basePrice,
        Integer duration,
        List<Long> featureIds,
        List<Line> lines,
        BigDecimal total
        ) {
    public record Line(String label, BigDecimal amount) {}
}
