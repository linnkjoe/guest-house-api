/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping("/rooms")
    public List<Room> retrieveRooms() {
        return roomService.retrieveAllRooms();
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping("/rooms/{id}")
    public EntityModel retrieveRoomById(@PathVariable Long id){
        return roomService.findRoomById(id);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping({"/rooms/available", "/rooms/avalable"})
    public List<Room> retrieveAvalableRooms(){
        return roomService.retriveAvalableRooms();
    }

    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room room){
    
        Room savedRoom = roomService.createRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
        
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update/room")
    public Room updateRoom(@Valid @RequestBody Room room){
        return roomService.updateRoom(room);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/clean")
    public void startCleaning(@PathVariable Long id) throws BusinessException {
        roomService.startCleaning(id);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping({"/{id}/available", "/{id}/avalable"})
    public ResponseEntity<Room> markRoomAsAvalable(@PathVariable Long id) throws BusinessException {
        return roomService.markRoomAsAvalable(id);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping({"/{id}/maintenance", "/{id}/maintence"})
    public ResponseEntity<Room> markRoomAsMaintencce(@PathVariable Long id) throws BusinessException {
        return roomService.markRoomAsMaintence(id);
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @GetMapping({"/rooms/occupied", "/rooms/ocupied"})
    public List<Room> retrieveOcupiedRooms(){
        return roomService.getOcupiedRooms();
    }
    
}
