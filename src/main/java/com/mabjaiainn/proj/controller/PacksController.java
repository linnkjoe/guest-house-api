/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.entity.Client;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.repository.PacksRepository;
import com.mabjaiainn.proj.service.PackService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class PacksController {
    PackService packService;


    public PacksController(PackService packService) {
        this.packService = packService;
    }
    
    @GetMapping("/packs")
    public List<Packs> retrieveAllPackages(){
    
        return packService.retrievePacks();
    }
    
    @GetMapping("/packs/{id}")
    public Packs retrieveClientByid(@PathVariable Long id){
    
        return packService.findPackById(id).get();
    }
    
    @DeleteMapping("/packs/{id}")
    public ResponseEntity<Packs> deleteById(@PathVariable Long id){
        packService.deleteById(id);
        return ResponseEntity.noContent().build();
        
    }
    
    @PostMapping("/packs")
    public ResponseEntity<Packs> createPack(@Valid @RequestBody Packs pack){
        Packs savedPack = packService.createPack(pack);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPack);
    }
    
}
