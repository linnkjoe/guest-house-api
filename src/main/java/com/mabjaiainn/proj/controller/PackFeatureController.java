package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.DTO.FeatureRequest;
import com.mabjaiainn.proj.entity.PackFeature;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.service.PackFeatureService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/features")
public class PackFeatureController {

    private final PackFeatureService service;

    public PackFeatureController(PackFeatureService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','CLIENTE')")
    @GetMapping
    public List<PackFeature> list(Authentication authentication) {
        boolean staff = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_GESTOR"));
        return service.list(!staff);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping
    public ResponseEntity<PackFeature> create(@Valid @RequestBody FeatureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PutMapping("/{id}")
    public PackFeature update(@PathVariable Long id, @Valid @RequestBody FeatureRequest request) {
        return service.update(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws BusinessException {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
