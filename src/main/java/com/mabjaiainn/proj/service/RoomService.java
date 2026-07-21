/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
import com.mabjaiainn.proj.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */
@Service
public class RoomService {

    private RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public EntityModel<Room> findRoomById(Long id) {
        Optional<Room> room = roomRepository.findById(id);
        if (room.isEmpty()) {
            throw new SourceNotFound("Room Not Found");
        }

        EntityModel<Room> entintyModel = EntityModel.of(room.get());
        return entintyModel;
    }

    public List<Room> retrieveAllRooms() {
        return roomRepository.findAll();
    }

    public Room createRoom(Room room) {

        return roomRepository.save(room);
    }

    public List<Room> retriveAvalableRooms() {
        return roomRepository.findByStatus(RoomStatus.DISPONIVEL);
    }

    @Transactional
    public Room updateRoom(Room room) {

        return roomRepository.save(room);
    }

    @Transactional
    public ResponseEntity<Room> startCleaning(Long roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        if(room == null){
            throw new SourceNotFound("Room Not Found");
        }
        room.setStatus(RoomStatus.EM_LIMPEZA);
        return ResponseEntity.ok(room);
    }

    @Transactional
    public ResponseEntity<Room> markRoomAsAvalable(Long roomId) {
        Room room = findRoomById(roomId).getContent();
        if (room == null) {
            throw new SourceNotFound("Room Not Found");
        }

        room.setStatus(RoomStatus.DISPONIVEL);
        return ResponseEntity.ok(room);
    }

    @Transactional
    public ResponseEntity<Room> markRoomAsMaintence(Long roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            throw new SourceNotFound("Room Not Found");
        }
        room.setStatus(RoomStatus.EM_MANUTENCAO);
        
        return ResponseEntity.ok(room);
    }
    
    public List<Room> getOcupiedRooms(){
        return roomRepository.findByStatus(RoomStatus.OCUPADO);
    }
    
    
}


