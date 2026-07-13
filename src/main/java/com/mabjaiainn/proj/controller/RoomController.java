/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.hateoas.EntityModel;
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
public class RoomController {

    RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/rooms")
    public List<Room> retrieveRooms() {
        return roomService.retrieveAllRooms();
    }
    
    @GetMapping("/rooms/{id}")
    public EntityModel retrieveRoomById(@PathVariable Long id){
        return roomService.findRoomById(id);
    }
    
    @GetMapping("/rooms/avalable")
    public List<Room> retrieveAvalableRooms(){
        return roomService.retriveAvalablePacks();
    }

    
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Room> deleteRoom(@PathVariable Long id){
            roomService.deleteById(id);
        return ResponseEntity.noContent().build();
              
    }
    
    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room room){
    
        Room savedRoom = roomService.createRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
        
    }
    
}
