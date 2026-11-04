/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.mabjaiainn.proj.DTO.ClientResponseDTO;
import com.mabjaiainn.proj.DTO.SetClientEnabledRequest;
import com.mabjaiainn.proj.exception.BusinessException;
import java.util.List;

import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.DTO.CreateClientRequest;
import com.mabjaiainn.proj.DTO.CreateClientResponse;
import com.mabjaiainn.proj.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author linnkjoe
 */
@RestController
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping("/clients")
    public List<ClientResponseDTO> retrieveAll(){
        return clientService.retrieveAllClients();
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR') or (hasRole('CLIENTE') and @guard.isClient(#id))")
    @GetMapping("/clients/{id}")
    public EntityModel<Client> retrieveById(@PathVariable Long id){
       return clientService.retrieveClientById(id);
}
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) throws BusinessException {
        clientService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PatchMapping("/clients/{id}/enabled")
    public ResponseEntity<Void> setEnabled(@PathVariable Long id, @Valid @RequestBody SetClientEnabledRequest request)
            throws BusinessException {
        clientService.setClientEnabled(id, request.enabled());
        return ResponseEntity.noContent().build();
    }
   
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping("/clients")
     public ResponseEntity<CreateClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) throws BusinessException{
         Client client = new Client(request.phoneNumber(), null,
                 request.clientFirstName(), request.clientLastName(), request.clientEmail());
         CreateClientResponse response = clientService.createClientWithTemporaryPassword(client);
         return ResponseEntity.status(HttpStatus.CREATED).body(response);
     }
}
