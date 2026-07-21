/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.DTO;

import com.mabjaiainn.proj.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author linnkjoe
 */
public record BookingResponseDTO (
        Long id,
        Long roomId,
        LocalDateTime bookingEnter,
        LocalDateTime bookingOut,
        Long clientId,
        BookingStatus status,
        BigDecimal advancedPrice,
        BigDecimal totalPrice,
        BigDecimal penalty
        ){}
    

