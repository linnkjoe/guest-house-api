package com.mabjaiainn.proj.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "about_page")
public class AboutPageContent {

    @Id
    private Long id;

    @Column(name = "page_title", nullable = false, length = 100)
    private String title;

    @Column(name = "introduction", nullable = false, length = 240)
    private String introduction;

    @Column(name = "page_description", nullable = false, length = 2000)
    private String description;

    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @Column(name = "contact", nullable = false, length = 30)
    private String contact;

    @Column(name = "image_content_type", length = 50)
    private String imageContentType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    @JsonIgnore
    public String getImageContentType() { return imageContentType; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }

    @Transient
    public String getImageUrl() {
        return imageContentType == null ? null : "/about/image";
    }
}
