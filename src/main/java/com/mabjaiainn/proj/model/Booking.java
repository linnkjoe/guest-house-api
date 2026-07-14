/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Generated;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author linnkjoe
 */
@Entity
public class Booking {
    @Id
    @GeneratedValue
    private Long bookingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="client_id", nullable=false)
    @JsonIgnore
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_id", nullable=false)
    @JsonIgnore
    private Room room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="pack_id", nullable=false)
    @JsonIgnore
    private Packs pack;
    
    
    @NotNull
    @FutureOrPresent(message ="A data de chack-in nao deve estar no passado")
    private LocalDateTime bookingEnter;
    
    private LocalDateTime bookingOut;
    
    private BigDecimal advancedPayment;
    private BigDecimal totalPrice;

    public Booking() {
    }

    
    
    public Booking(LocalDateTime bookingEnter) {
        this.bookingEnter = bookingEnter;
       
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Packs getPack() {
        return pack;
    }

    public void setPack(Packs pack) {
        this.pack = pack;
    }

    public LocalDateTime getBookingEnter() {
        return bookingEnter;
    }

    public void setBookingEnter(LocalDateTime bookingEnter) {
        this.bookingEnter = bookingEnter;
    }

    public LocalDateTime getBookingOut() {
        return bookingOut;
    }

    public void setBookingOut(LocalDateTime bookingOut) {
        this.bookingOut = bookingOut;
    }

    public BigDecimal getAdvancedPayment() {
        return advancedPayment;
    }

    public void setAdvancedPayment(BigDecimal advancedPayment) {
        this.advancedPayment = advancedPayment;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

        
    
    
}
