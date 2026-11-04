package com.mabjaiainn.proj.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CustomPackResponse(
        Long id,
        String name,
        Integer duration,
        List<Long> featureIds,
        List<String> featureNames,
        BigDecimal price,
        boolean priceChanged,
        Integer useCount,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt
        ) {}
