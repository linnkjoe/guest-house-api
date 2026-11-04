/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mabjaiainn.proj.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.mabjaiainn.proj.entity.PackImage;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.service.PackService;
import jakarta.validation.Valid;
import java.util.List;
import com.mabjaiainn.proj.DTO.PackRequest;
import com.mabjaiainn.proj.exception.BusinessException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author linnkjoe
 */
@RestController
public class PacksController {
    PackService packService;


    public PacksController(PackService packService) {
        this.packService = packService;
    }
    
    @GetMapping("/packs")
    public List<Packs> retrieveAllPackages(){
        return packService.retrievePacks();
    }
    
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR','CLIENTE')")
    @GetMapping("/packs/{id}")
    public Packs retrieveClientByid(@PathVariable Long id){
        return packService.findPackById(id).get();
    }

    @GetMapping("/packs/{id}/image")
    public ResponseEntity<byte[]> getPackImage(@PathVariable Long id) {
        Packs pack = packService.findPackById(id)
                .orElseThrow(() -> new com.mabjaiainn.proj.exception.SourceNotFound("Pack Not Found"));
        PackImage packImage = packService.findImageByPackId(id).orElse(null);
        if (packImage == null || pack.getImageContentType() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(pack.getImageContentType()))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noCache())
                .body(packImage.getImageData());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/packs/{id}")
    public ResponseEntity<Packs> deleteById(@PathVariable Long id) throws BusinessException {
        packService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping("/packs")
    public ResponseEntity<Packs> createPack(@Valid @RequestBody PackRequest request) throws BusinessException {
        Packs savedPack = packService.createPack(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPack);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PostMapping(value = "/packs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Packs> createPackWithImage(
            @Valid @RequestPart("request") PackRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws BusinessException {
        Packs savedPack = packService.createPack(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPack);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PutMapping("/packs/{id}")
    public Packs updatePack(@PathVariable Long id, @Valid @RequestBody PackRequest request) throws BusinessException {
        return packService.updatePack(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    @PutMapping(value = "/packs/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Packs updatePackWithImage(
            @PathVariable Long id,
            @Valid @RequestPart("request") PackRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws BusinessException {
        return packService.updatePack(id, request, image);
    }
}
