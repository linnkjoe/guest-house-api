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
import com.mabjaiainn.proj.service.BookingService;
import com.mabjaiainn.proj.service.ClientService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    ClientService clientService;

    
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
               
    }
    
    @GetMapping("/clients")
    public List<Client> retrieveAll(){
        return clientService.retrieveAllClients();
    }
    
    @GetMapping("/clients/{id}")
    public EntityModel<Client> retrieveById(@PathVariable Long id){
       return clientService.retrieveClientById(id);
}
    
    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable Long id){
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
   
    @PostMapping("/clients")
     public ResponseEntity<Client> createClient(@Valid @RequestBody Client client){
         Client savedClient = clientService.createClient(client);
         return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
     }
     
     
     
     
    
}

