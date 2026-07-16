/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.BookingResponseDTO;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.model.Booking;
import com.mabjaiainn.proj.model.Client;
import com.mabjaiainn.proj.model.Packs;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.repository.ClientRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */
@Service
public class ClientService {

   private ClientRepository clientRepository;
   
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    

    public List<Client> retrieveAllClients() {
        return clientRepository.findAll();
    }

    public EntityModel<Client> retrieveClientById(Long id) {
        Optional<Client> client = clientRepository.findById(id);

        if (client.isEmpty()) {
            throw new SourceNotFound("Client Not found");
        }
        EntityModel<Client> entityModel = EntityModel.of(client.get());
        return entityModel;

    }

    public void deleteById(Long id) {
         Optional<Client> client = clientRepository.findById(id);

        if (client.isEmpty()) {
            throw new SourceNotFound("Client Not Found");
        }
        clientRepository.deleteById(id);
    }

    public Client createClient(Client client){
    
              return clientRepository.save(client);

    }   
        
    }


