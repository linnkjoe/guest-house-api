/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.DTO.BookingResponseDTO;
import com.mabjaiainn.proj.service.BookingService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
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
    
    @GetMapping("/{id}/booking")
    public BookingResponseDTO retrieveBookingById(@PathVariable Long id) throws BusinessException{
    
        return bookingService.getBookingById(id);
    }

    @PostMapping("/booking/{clientId}/{packId}")
    public BookingResponseDTO createBooking(@Valid @RequestBody Booking booking,
            @PathVariable Long clientId,
            @PathVariable Long packId) throws BusinessException {
        return bookingService.createBookingForClient(booking, clientId, packId);
    }

    @PostMapping("/{id}/check-in")
    public BookingResponseDTO checkIn(@PathVariable Long id) throws BusinessException  {
        return bookingService.checkIn(id);
    }
    
    @PostMapping("/{id}/check-out")
    public BookingResponseDTO checkOut(@PathVariable Long id) throws BusinessException{
        return bookingService.checkOut(id);
    }
    
    @PostMapping("/{id}/cancel")
    public BookingResponseDTO cancelBooking(@PathVariable Long id) throws BusinessException{
     return bookingService.cancelBooking(id);
    }
    
    @PostMapping("/{id}/extend/{newCheckOut}")
    public BookingResponseDTO extendBooking(@PathVariable Long id, 
            @PathVariable LocalDateTime newCheckOut) throws BusinessException{
        return bookingService.extendBooking(id, newCheckOut);
    }
    
    @GetMapping("/future-bookings")
    public List<BookingResponseDTO> retrieveFutureBookings(){
        return bookingService.getFutureBooking();
    }
   
    @GetMapping("/client/{id}/bookings")
    public List<BookingResponseDTO> retrieveClientBookingHistory(@PathVariable Long id){
        return bookingService.getBookingHistoryByGuest(id);
    }
    
    @GetMapping("/client/{id}/bookings-current")
    public List<BookingResponseDTO> retrieveClientCurrentBookings(@PathVariable Long id){
        return bookingService.getCurrentBooking(id);
    }
    
}
