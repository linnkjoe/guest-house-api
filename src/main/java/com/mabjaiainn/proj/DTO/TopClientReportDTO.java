package com.mabjaiainn.proj.DTO;

import java.math.BigDecimal;

public record TopClientReportDTO(
        Long clientId,
        String clientName,
        Long bookingCount,
        BigDecimal totalSpent
) {
}
