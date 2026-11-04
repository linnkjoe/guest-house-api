package com.mabjaiainn.proj.controller;

import com.mabjaiainn.proj.DTO.AboutPageRequest;
import com.mabjaiainn.proj.entity.AboutPageContent;
import com.mabjaiainn.proj.entity.AboutPageImage;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.service.AboutPageService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AboutPageController {

    private final AboutPageService aboutPageService;

    public AboutPageController(AboutPageService aboutPageService) {
        this.aboutPageService = aboutPageService;
    }

    @GetMapping("/about")
    public AboutPageContent getContent() {
        return aboutPageService.getContent();
    }

    @GetMapping("/about/image")
    public ResponseEntity<byte[]> getImage() {
        AboutPageContent content = aboutPageService.getContent();
        AboutPageImage image = aboutPageService.getImage()
                .orElseThrow(() -> new SourceNotFound("About page image not found"));
        if (content.getImageContentType() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(content.getImageContentType()))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noCache())
                .body(image.getImageData());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/about", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AboutPageContent> update(
            @Valid @RequestPart("request") AboutPageRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws BusinessException {
        return ResponseEntity.status(HttpStatus.OK).body(aboutPageService.update(request, image));
    }
}
