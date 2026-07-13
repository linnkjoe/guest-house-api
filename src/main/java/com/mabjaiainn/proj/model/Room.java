/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 *
 * @author linnkjoe
 */
@Entity
public class Room {
    
    @Id
    @GeneratedValue
    private Long roomId;
    
    @NotNull(message="O numero do quarto e obriatorio")
    private Integer roomNumber;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;
    
    @Column(name= "data_fim_limpeza")
    private java.time.LocalDateTime dataFimLimpeza;
    public Room() {
    }

    
    
    public Room(Integer roomNumber, RoomStatus status) {
        this.roomNumber = roomNumber;
        this.status = status;
    }

    public LocalDateTime getDataFimLimpeza() {
        return dataFimLimpeza;
    }

    public void setDataFimLimpeza(LocalDateTime dataFimLimpeza) {
        this.dataFimLimpeza = dataFimLimpeza;
    }

    
    
    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int room_number) {
        this.roomNumber = roomNumber;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
    
    
}
