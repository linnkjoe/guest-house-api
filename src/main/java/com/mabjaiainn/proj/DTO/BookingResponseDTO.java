/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.DTO;

import com.mabjaiainn.proj.entity.BookingStatus;
import com.mabjaiainn.proj.entity.PaymentMethod;
import com.mabjaiainn.proj.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author linnkjoe
 */
public record BookingResponseDTO (
        Long id,
        Long roomId,
        Integer roomNumber,
        LocalDateTime bookingEnter,
        LocalDateTime bookingOut,
        Long clientId,
        BookingStatus status,
        BigDecimal advancedPrice,
        BigDecimal totalPrice,
        BigDecimal penalty,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
        ){}
    
