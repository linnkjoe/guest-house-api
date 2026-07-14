/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.model.Booking;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.model.RoomStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author linnkjoe
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {


    List<Booking> findByRoom(Room room);
    Booking findByBookingId(Long bookingId);
    
    
}
