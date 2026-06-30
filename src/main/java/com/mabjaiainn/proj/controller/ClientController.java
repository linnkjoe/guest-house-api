/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.model.Booking;
import java.util.List;

import com.mabjaiainn.proj.model.Client;
import com.mabjaiainn.proj.model.Packs;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.repository.BookingRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mabjaiainn.proj.repository.ClientRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import com.mabjaiainn.proj.repository.RoomRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author linnkjoe
 */
@RestController
public class ClientController {
    private ClientRepository repository;
    private PacksRepository packRepository;
    private RoomRepository roomRepository;
    private BookingRepository boockingRepository;
    
    public ClientController(ClientRepository repository, PacksRepository packRepository, RoomRepository roomRepository, BookingRepository boockingRepository) {
        this.repository = repository;
        this.packRepository = packRepository;
        this.roomRepository = roomRepository;
        this.boockingRepository = boockingRepository;       
    }
    
    @GetMapping("/clients")
    public List<Client> retrieveAll(){
        return repository.findAll();
    }
    
    @GetMapping("/clients/{id}")
    public EntityModel<Client> retrieveById(@PathVariable Long id){
        Optional<Client> client = repository.findById(id);

        if (client.isEmpty()) {
            throw new SourceNotFound("id:" + id);
        }
        EntityModel<Client> entityModel = EntityModel.of(client.get());
        return entityModel;
}
    
    @DeleteMapping("/clients/{id}")
    public void deleteById(@PathVariable Long id){
        repository.deleteById(id);
    }
    
    @GetMapping("clients/{id}/bookings")
    public List<Booking> retrieveBookingForUser(@PathVariable Long id){
        Optional<Client> client = repository.findById(id);

        if (client.isEmpty()) {
            throw new SourceNotFound("id:" + id);
        }
        
        return client.get().getBooking();
        
    }
    
    @PostMapping("/client/{clientId}/packs/{packId}/room/{roomId}/bookings")
    public Booking createBooking(@PathVariable Long clientId,@PathVariable Long packId, 
            @PathVariable Long roomId , @RequestBody Booking booking){
        Optional<Client> client = repository.findById(clientId);
         Optional<Packs> pack = packRepository.findById(packId);
         Optional<Room> room = roomRepository. findById(roomId);
         
        if (client.isEmpty()) {
            throw new SourceNotFound("id:" + clientId);
            
        }
        
        if (pack.isEmpty()) {
            throw new SourceNotFound("id:" + packId);
            
        }
        
        if (room.isEmpty()) {
            throw new SourceNotFound("id:" + roomId);
            
        }
        
      booking.setClient(client.get());
      booking.setPack(pack.get());
      booking.setRoom(room.get());
       boockingRepository.save(booking);
      
      return booking;
    }


    
}

