/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.entity.Booking;
import com.mabjaiainn.proj.entity.PaymentMethod;
import com.mabjaiainn.proj.DTO.BookingResponseDTO;
import com.mabjaiainn.proj.DTO.BookingRequest;
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
import org.springframework.security.core.Authentication;

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

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping("/bookings")
    public List<BookingResponseDTO> retrieveBookings() {
        return bookingService.retrieveAllBookings();
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR') or (hasRole('CLIENTE') and @guard.ownsBooking(#id))")
    @GetMapping("/{id}/booking")
    public BookingResponseDTO retrieveBookingById(@PathVariable Long id) throws BusinessException{
    
        return bookingService.getBookingById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR') or (hasRole('CLIENTE') and @guard.isClient(#clientId))")
    @PostMapping("/booking/{clientId}/{packId}")
    public BookingResponseDTO createBooking(@Valid @RequestBody BookingRequest request,
            @PathVariable Long clientId,
            @PathVariable Long packId,
            Authentication authentication) throws BusinessException {
        boolean clientRequest = isClient(authentication);
        PaymentMethod paymentMethod = clientRequest ? request.paymentMethod() : null;
        if (clientRequest && paymentMethod == null) {
            throw new BusinessException("Payment method is required for client bookings");
        }
        return bookingService.createBookingForClient(
                new Booking(request.bookingEnter()), clientId, packId, paymentMethod);
    }

    private boolean isClient(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CLIENTE"));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping("/{id}/check-in")
    public BookingResponseDTO checkIn(@PathVariable Long id) throws BusinessException  {
        return bookingService.checkIn(id);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping("/{id}/check-out")
    public BookingResponseDTO checkOut(@PathVariable Long id) throws BusinessException{
        return bookingService.checkOut(id);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR') or (hasRole('CLIENTE') and @guard.ownsBooking(#id))")
    @PostMapping("/{id}/cancel")
    public BookingResponseDTO cancelBooking(@PathVariable Long id) throws BusinessException{
     return bookingService.cancelBooking(id);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR') or (hasRole('CLIENTE') and @guard.ownsBooking(#id))")
    @PostMapping("/{id}/extend/{newCheckOut}")
    public BookingResponseDTO extendBooking(@PathVariable Long id, 
            @PathVariable LocalDateTime newCheckOut) throws BusinessException{
        return bookingService.extendBooking(id, newCheckOut);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping("/future-bookings")
    public List<BookingResponseDTO> retrieveFutureBookings(){
        return bookingService.getFutureBooking();
    }
   
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR') or (hasRole('CLIENTE') and @guard.isClient(#id))")
    @GetMapping("/client/{id}/bookings")
    public List<BookingResponseDTO> retrieveClientBookingHistory(@PathVariable Long id){
        return bookingService.getBookingHistoryByGuest(id);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR') or (hasRole('CLIENTE') and @guard.isClient(#id))")
    @GetMapping("/client/{id}/bookings-current")
    public List<BookingResponseDTO> retrieveClientCurrentBookings(@PathVariable Long id){
        return bookingService.getCurrentBooking(id);
    }
    
}
