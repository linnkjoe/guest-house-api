package com.mabjaiainn.proj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "pack_image")
public class PackImage {

    @Id
    @Column(name = "pack_id")
    private Long packId;

    @Lob
    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    public PackImage() {
    }

    public PackImage(Long packId, byte[] imageData) {
        this.packId = packId;
        this.imageData = imageData;
    }

    public Long getPackId() {
        return packId;
    }

    public void setPackId(Long packId) {
        this.packId = packId;
    }

    @JsonIgnore
    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}
