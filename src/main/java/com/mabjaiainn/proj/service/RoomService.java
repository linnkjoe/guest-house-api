/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.model.Room;
import com.mabjaiainn.proj.repository.RoomRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 *
 * @author linnkjoe
 */

@Service
public class RoomService {
   private RoomRepository roomRepository;

    public RoomService() {
    }

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }
    
    public Optional<Room> findRoomById(Long id){
        Optional<Room> room = roomRepository.findById(id);
        if(room.isEmpty()){
        throw new SourceNotFound("id" + id);
        }
        
        return room;
    }
}
