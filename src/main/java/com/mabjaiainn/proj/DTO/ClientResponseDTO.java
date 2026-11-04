package com.mabjaiainn.proj.DTO;

public record ClientResponseDTO(
        Long clientId,
        Integer phoneNumber,
        String clientFirstName,
        String clientLastName,
        String clientEmail,
        boolean enabled) {
}
