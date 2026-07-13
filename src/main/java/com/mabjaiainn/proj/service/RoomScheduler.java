/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.model.Booking;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.model.RoomStatus;
import com.mabjaiainn.proj.repository.BookingRepository;
import com.mabjaiainn.proj.repository.RoomRepository;
import jakarta.transaction.Transactional;
import static java.time.Clock.fixed;
import java.time.Instant;
import static java.time.InstantSource.fixed;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author linnkjoe
 */

@Component
public class RoomScheduler {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public RoomScheduler(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }
    
    @Scheduled(fixedRate = 6000)
    @Transactional
    public void verifyRealeseRoom(){
        LocalDateTime nowLocal = LocalDateTime.now();
        
        // 1. Processar quartos que terminaram a estadia
        List<Booking> finishedBookings = bookingRepository.findBookingsTerminados(nowLocal);
        for (Booking booking : finishedBookings){
            Room room = booking.getRoom();
            room.setStatus(RoomStatus.EM_LIMPEZA);
            room.setDataFimLimpeza(nowLocal.plusMinutes(30)); // Alinhado para LocalDateTime
            roomRepository.save(room);
        }
        
        // 2. Processar quartos que terminaram a limpeza
        List<Room> roomsToRealese = roomRepository.findRoomsCleanFinished(nowLocal);
        for(Room room : roomsToRealese){
            room.setStatus(RoomStatus.DISPONIVEL);
            room.setDataFimLimpeza(null);
            roomRepository.save(room);
        }
    }
}
