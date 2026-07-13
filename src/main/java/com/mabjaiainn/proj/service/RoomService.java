/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.model.Packs;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.model.RoomStatus;
import com.mabjaiainn.proj.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.hateoas.EntityModel;
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
            throw new SourceNotFound("id" + id);
        }

        EntityModel<Room> entintyModel = EntityModel.of(room.get());
        return entintyModel;
    }

    public List<Room> retrieveAllRooms() {
        return roomRepository.findAll();
    }

    public void deleteById(Long id) {
        Optional<Room> room = roomRepository.findById(id);
        if (room.isEmpty()) {
            throw new SourceNotFound("id" + id);
        }

        roomRepository.delete(room.get());
    }

    public Room createRoom(Room room) {

        return roomRepository.save(room);
    }

    public List<Room> retriveAvalablePacks() {
        return roomRepository.findByStatus(RoomStatus.DISPONIVEL);
    }

    @Transactional
    public Room updateRoom(Room room) {

        return roomRepository.save(room);
    }

    @Transactional
    public void startCleaning(Long roomId) {
        Room room = this.findRoomById(roomId).getContent();

        room.setStatus(RoomStatus.EM_LIMPEZA);

        room.setDataFimLimpeza(LocalDateTime.now().plusMinutes(30));

        roomRepository.save(room);
    }
}
