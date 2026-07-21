/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.DTO;

import com.mabjaiainn.proj.entity.BookingStatus;
import com.mabjaiainn.proj.entity.RoomStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author linnkjoe
 */

    public record RoomResponseDTO (
        Long roomId,
        Integer roomNumber,
        RoomStatus roomStatus
        ){}

