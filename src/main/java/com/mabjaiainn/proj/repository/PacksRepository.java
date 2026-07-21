/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.entity.Packs;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author linnkjoe
 */
public interface PacksRepository extends JpaRepository<Packs, Long> {
    
}
