/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.model.Booking;
import com.mabjaiainn.proj.service.BookingResponseDTO;
import com.mabjaiainn.proj.service.BookingService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author linnkjoe
 */
@RestController
public class BookingController {

    private BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings")
    public List<Booking> retrieveBookings() {
        return bookingService.retrieveAllBookings();
    }

    @PostMapping("/booking/{clientId}/{packId}")
    public BookingResponseDTO createBooking(@RequestBody Booking booking,
            @PathVariable Long clientId,
            @PathVariable Long packId) throws BusinessException, BusinessException {
        return bookingService.createBookingForClient(booking, clientId, packId);
    }

    @PostMapping("/{id}/check-in")
    public BookingResponseDTO checkIn(@PathVariable Long id) throws BusinessException {
        return bookingService.checkIn(id);
    }
}
