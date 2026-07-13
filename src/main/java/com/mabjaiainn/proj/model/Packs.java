/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 *
 * @author linnkjoe
 */
@Entity
public class Packs {
    @Id
    @GeneratedValue
    private Long packId;
    
    @Size(min=2)
    private String packName;
    
    @NotNull(message="A duracao e obrigatoria")
    @Min(value=1, message="Pacotes tem duracao minima duma hora")
    @Column(name="pack_duration")
    private Integer duration;
    
    @NotNull
    private Boolean packAc;
    
    @NotNull(message = "O preco do pacote e obrigatorio")
    @Column(name="pack_price")
    private BigDecimal packPrice;

    public Packs() {
    }
    
    

    public Packs(String packName, Integer duration, Boolean packAc, BigDecimal packPrice) {
        this.packName = packName;
        this.duration = duration;
        this.packAc = packAc;
        this.packPrice = packPrice;
    }

    public Long getPackId() {
        return packId;
    }

    public void setPackId(Long packId) {
        this.packId = packId;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getPackAc() {
        return packAc;
    }

    public void setPackAc(Boolean packAc) {
        this.packAc = packAc;
    }

    public BigDecimal getPackPrice() {
        return packPrice;
    }

    public void setPackPrice(BigDecimal packPrice) {
        this.packPrice = packPrice;
    }
    
    
}
