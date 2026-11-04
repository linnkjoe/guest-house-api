package com.mabjaiainn.proj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "about_page_image")
public class AboutPageImage {

    @Id
    private Long id;

    @Lob
    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    public AboutPageImage() {
    }

    public AboutPageImage(Long id, byte[] imageData) {
        this.id = id;
        this.imageData = imageData;
    }

    public Long getId() { return id; }

    @JsonIgnore
    public byte[] getImageData() { return imageData; }

    public void setImageData(byte[] imageData) { this.imageData = imageData; }
}
