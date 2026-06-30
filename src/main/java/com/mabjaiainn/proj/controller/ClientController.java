/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import java.util.List;
import com.mabjaiainn.proj.dao.ClientDao;
import com.mabjaiainn.proj.model.Client;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author linnkjoe
 */
@RestController
public class ClientController {
    private ClientDao repository;

    public ClientController(ClientDao repository) {
        this.repository = repository;
    }
    
    @GetMapping("/clients")
    public List<Client> retrieveAll(){
        return repository.findAll();
    }
    
    
}
