/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;

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

    @Column(name = "pack_description", length = 500)
    private String description;

    @Column(name = "request_count", nullable = false)
    private Long requestCount = 0L;

    @Column(name = "promotion_discount", nullable = false, precision = 5, scale = 2)
    private BigDecimal promotionDiscount = BigDecimal.ZERO;

    @Column(name = "image_content_type", length = 50)
    private String imageContentType;

    // true = copia criada para uma reserva personalizada (nao aparece na lista de estadias).
    @Column(name = "pack_custom", nullable = false)
    private Boolean custom = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "pack_feature_link",
            joinColumns = @JoinColumn(name = "pack_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id"))
    private Set<PackFeature> features = new LinkedHashSet<>();

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
    
    

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getRequestCount() { return requestCount; }
    public void setRequestCount(Long requestCount) { this.requestCount = requestCount; }
    public BigDecimal getPromotionDiscount() { return promotionDiscount; }
    public void setPromotionDiscount(BigDecimal promotionDiscount) { this.promotionDiscount = promotionDiscount; }
    @Transient
    public BigDecimal getDiscountedPrice() {
        BigDecimal discount = promotionDiscount == null ? BigDecimal.ZERO : promotionDiscount;
        if (discount.signum() == 0) {
            return packPrice;
        }
        return packPrice.multiply(BigDecimal.ONE.subtract(discount.movePointLeft(2)))
                .setScale(2, RoundingMode.HALF_UP);
    }
    @Transient
    public String getImageUrl() {
        return imageContentType == null || packId == null ? null : "/packs/" + packId + "/image";
    }
    @JsonIgnore
    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }
    public Boolean getCustom() { return custom; }
    public void setCustom(Boolean custom) { this.custom = custom; }
    public Set<PackFeature> getFeatures() { return features; }
    public void setFeatures(Set<PackFeature> features) { this.features = features; }
}
