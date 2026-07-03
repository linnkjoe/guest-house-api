/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.repository.RoomRepository;
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
    
    public void deleteById(Long id){
        Optional<Room> room = roomRepository.findById(id);
        if (room.isEmpty()) {
            throw new SourceNotFound("id" + id);
        }
        
        roomRepository.delete(room.get());
    }
    
    public Room createRoom(Room room){
    
        return roomRepository.save(room);
    }

}
