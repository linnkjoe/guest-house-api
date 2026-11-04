package com.mabjaiainn.proj.repository;

import com.mabjaiainn.proj.entity.CustomPackage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomPackageRepository extends JpaRepository<CustomPackage, Long> {
    List<CustomPackage> findByClient_ClientIdOrderByLastUsedAtDescCreatedAtDesc(Long clientId);
    Optional<CustomPackage> findByCustomIdAndClient_ClientId(Long customId, Long clientId);
    boolean existsByClient_ClientIdAndNameIgnoreCase(Long clientId, String name);
    long countByClient_ClientId(Long clientId);
    void deleteByClient_ClientId(Long clientId);
}
