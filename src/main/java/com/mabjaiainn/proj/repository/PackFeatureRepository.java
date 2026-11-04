package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.entity.PackFeature;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackFeatureRepository extends JpaRepository<PackFeature, Long> {
    List<PackFeature> findByActiveTrueOrderByNameAsc();
    List<PackFeature> findAllByOrderByNameAsc();
    Optional<PackFeature> findByCode(String code);
    boolean existsByCode(String code);
}
