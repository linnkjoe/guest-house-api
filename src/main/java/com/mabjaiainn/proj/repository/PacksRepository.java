/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.entity.Packs;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author linnkjoe
 */
public interface PacksRepository extends JpaRepository<Packs, Long> {

    /** Estadias do catalogo (exclui as copias criadas para reservas personalizadas). */
    List<Packs> findByCustomFalseOrderByPackPriceAscDurationAsc();

    boolean existsByPackNameIgnoreCaseAndCustomFalse(String packName);

    boolean existsByPackNameIgnoreCaseAndCustomFalseAndPackIdNot(String packName, Long packId);

    @Modifying
    @Transactional
    @Query("UPDATE Packs p SET p.requestCount = p.requestCount + 1 WHERE p.packId = :packId")
    int incrementRequestCount(@Param("packId") Long packId);
}
