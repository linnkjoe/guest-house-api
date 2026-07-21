/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
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

    List<Booking> findByClient(Client client);

    List<Booking> findBookingByBookingEnterBetween(LocalDateTime bookingEnter, LocalDateTime bookingOut);

}
