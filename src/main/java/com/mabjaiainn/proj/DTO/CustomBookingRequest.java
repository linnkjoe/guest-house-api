package com.mabjaiainn.proj.DTO;

import com.mabjaiainn.proj.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CustomBookingRequest(
        @NotNull(message = "A data de chegada e obrigatoria") LocalDateTime bookingEnter,
        PaymentMethod paymentMethod) {}
