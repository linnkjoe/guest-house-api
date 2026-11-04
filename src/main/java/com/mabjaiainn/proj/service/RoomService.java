/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.entity.Room;
import com.mabjaiainn.proj.entity.RoomStatus;
import com.mabjaiainn.proj.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.mabjaiainn.proj.exception.BusinessException;
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

    @Transactional
    public List<Room> findReservableRoomsForBooking() {
        return roomRepository.findReservableRoomsForUpdate(
                List.of(RoomStatus.DISPONIVEL, RoomStatus.RESERVADO, RoomStatus.OCUPADO));
    }

    public Room createRoom(Room room) {
        room.setStatus(RoomStatus.DISPONIVEL);
        room.setDataFimLimpeza(null);
        return roomRepository.save(room);
    }

    public List<Room> retriveAvalableRooms() {
        return roomRepository.findByStatus(RoomStatus.DISPONIVEL);
    }

    @Transactional
    public Room updateRoom(Room room) {
        Room existing = roomRepository.findByRoomId(room.getRoomId());
        if (existing == null) {
            throw new SourceNotFound("Room Not Found");
        }
        existing.setRoomNumber(room.getRoomNumber());
        return existing;
    }

    @Transactional
    public Room reserveRoom(Long roomId) throws BusinessException {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            throw new SourceNotFound("Room Not Found");
        }
        if (room.getStatus() != RoomStatus.DISPONIVEL && room.getStatus() != RoomStatus.RESERVADO) {
            throw new BusinessException("Room is not available");
        }
        room.setStatus(RoomStatus.RESERVADO);
        return room;
    }

    @Transactional
    public ResponseEntity<Room> startCleaning(Long roomId) throws BusinessException {
        Room room = roomRepository.findByRoomId(roomId);
        if(room == null){
            throw new SourceNotFound("Room Not Found");
        }
        if (room.getStatus() == RoomStatus.OCUPADO || room.getStatus() == RoomStatus.RESERVADO) {
            throw new BusinessException("Room has an active booking");
        }
        room.setStatus(RoomStatus.EM_LIMPEZA);
        room.setDataFimLimpeza(LocalDateTime.now().plusMinutes(30));
        return ResponseEntity.ok(room);
    }

    @Transactional
    public ResponseEntity<Room> markRoomAsAvalable(Long roomId) throws BusinessException {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            throw new SourceNotFound("Room Not Found");
        }
        if (room.getStatus() == RoomStatus.OCUPADO || room.getStatus() == RoomStatus.RESERVADO) {
            throw new BusinessException("Room has an active booking");
        }
        room.setStatus(RoomStatus.DISPONIVEL);
        room.setDataFimLimpeza(null);
        return ResponseEntity.ok(room);
    }

    @Transactional
    public ResponseEntity<Room> markRoomAsMaintence(Long roomId) throws BusinessException {
        Room room = roomRepository.findByRoomId(roomId);
        if (room == null) {
            throw new SourceNotFound("Room Not Found");
        }
        if (room.getStatus() == RoomStatus.OCUPADO || room.getStatus() == RoomStatus.RESERVADO) {
            throw new BusinessException("Room has an active booking");
        }
        room.setStatus(RoomStatus.EM_MANUTENCAO);
        room.setDataFimLimpeza(null);
        
        return ResponseEntity.ok(room);
    }
    
    public List<Room> getOcupiedRooms(){
        return roomRepository.findByStatus(RoomStatus.OCUPADO);
    }
    
    
}
