/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.model.Booking;
import com.mabjaiainn.proj.repository.BookingRepository;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */
@Service
public class BookingService {
    private BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    
   public Booking saveBooking(Booking booking){
      return bookingRepository.save(booking);
   }

   
}
