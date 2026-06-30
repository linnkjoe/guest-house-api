/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mabjaiainn.proj.dao;

import com.mabjaiainn.proj.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author linnkjoe
 */
public interface ClientDao extends JpaRepository<Client, Long> {
    
}
