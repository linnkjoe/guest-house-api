/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.model.Booking;
import com.mabjaiainn.proj.model.Client;
import com.mabjaiainn.proj.model.Packs;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.model.RoomStatus;
import com.mabjaiainn.proj.repository.BookingRepository;
import com.mabjaiainn.proj.repository.ClientRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import com.mabjaiainn.proj.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */
@Service
public class BookingService {
    private BookingRepository bookingRepository;
    private PackService packService;
    private RoomService roomService;
    private ClientService clientService;

    public BookingService(BookingRepository bookingRepository, PackService packService, RoomService roomService, ClientService clientService) {
        this.bookingRepository = bookingRepository;
        this.packService = packService;
        this.roomService = roomService;
        this.clientService = clientService;
    }

    
   @Transactional 
   public Booking updateBooking(Booking booking){
      return bookingRepository.save(booking);
   }
   
   public List<Booking> retrieveAllBookings(){
       return bookingRepository.findAll();
   }
   
   public List<Booking> retrieveActiveBookings(){
       return bookingRepository.findByRoomStatus(RoomStatus.OCUPADO);
   }
   

   @Transactional
   public Booking createBookingForClient(Booking booking, Long clientId, Long roomId, Long packId){
        Client client = clientService.retrieveClientById(clientId).getContent();
        Room room = roomService.findRoomById(roomId).getContent();
        Packs pack = packService.findPackById(packId).
                orElseThrow(()-> new IllegalArgumentException("Pacote invalido recebido do servidor."));

        booking.setPack(pack);
        
        booking.setClient(client);
        booking.setRoom(room);
        
        Integer duration = pack.getDuration();
        LocalDateTime endTime = booking.getBookingEnter().plusHours(duration);
        booking.setBookingOut(endTime);
        
        room.setStatus(RoomStatus.OCUPADO);
        roomService.updateRoom(room);
        
        BigDecimal packPrice = pack.getPackPrice();
        booking.setAdvancedPayment(packPrice.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));

        booking.setTotalPrice(packPrice);
        
        
        return bookingRepository.save(booking);
        
       
   }
   
}
