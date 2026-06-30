/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 *
 * @author linnkjoe
 */
@Entity
public class Client {
    @Id
    @GeneratedValue
    private Long clientId;
    
    @NotNull(message= "O numero e obrigatorio")
    @Min(value=100000000, message = "O numero deve ter pelo menos 9 algarismos")
    @Max(value=999999999, message= "O numero deve ter no maximo 9 algarismos")
    private Integer phoneNumber;
    
    @Size(min=8)
    private String clientPassword;
    
    @Size(min=2)
    @Column(name="client_firstname")
    private String clientFirstName;
    @Size(min=2)
    
    @Column(name="client_lastname")
    private String clientLastName;
    
    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message="Insira um email valido")
    @Size(max=100, message = "O email nao pode ter mais de 100 caracteres")
    private String clientEmail;
    
    @OneToMany(mappedBy ="client")
    @JsonIgnore
    private List<Booking> booking;

    public Client() {
    }

    public List<Booking> getBooking() {
        return booking;
    }
    
    

    public void setBooking(List<Booking> booking) {
        this.booking = booking;
    }

    
    
    
    public Client(Integer phoneNumber, String clientPassword, String clientFirstName, String clientLastName, String clientEmail) {
        this.phoneNumber = phoneNumber;
        this.clientPassword = clientPassword;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.clientEmail = clientEmail;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Integer getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Integer phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    public String getClientFirstName() {
        return clientFirstName;
    }

    public void setClientFirstName(String clientFirstName) {
        this.clientFirstName = clientFirstName;
    }

    public String getClientLastName() {
        return clientLastName;
    }

    public void setClientLastName(String clientLastName) {
        this.clientLastName = clientLastName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }
    
    

    
}