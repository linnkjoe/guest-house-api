/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.model.BookingStatus;
import java.time.LocalDateTime;

/**
 *
 * @author linnkjoe
 */
public record BookingResponseDTO (
        Long id,
        LocalDateTime bookingEnter,
        LocalDateTime bookingOut,
        Long clientId,
        Integer roomNumber,
        BookingStatus status
        ){}
    

