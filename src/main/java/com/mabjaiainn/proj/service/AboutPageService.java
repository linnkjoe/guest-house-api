package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.AboutPageRequest;
import com.mabjaiainn.proj.entity.AboutPageContent;
import com.mabjaiainn.proj.entity.AboutPageImage;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.repository.AboutPageContentRepository;
import com.mabjaiainn.proj.repository.AboutPageImageRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AboutPageService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 4096;
    private static final long PAGE_ID = 1L;

    private final AboutPageContentRepository contentRepository;
    private final AboutPageImageRepository imageRepository;

    public AboutPageService(AboutPageContentRepository contentRepository, AboutPageImageRepository imageRepository) {
        this.contentRepository = contentRepository;
        this.imageRepository = imageRepository;
    }

    public AboutPageContent getContent() {
        return contentRepository.findById(PAGE_ID)
                .orElseThrow(() -> new SourceNotFound("About page content not found"));
    }

    public Optional<AboutPageImage> getImage() {
        return imageRepository.findById(PAGE_ID);
    }

    @Transactional
    public AboutPageContent update(AboutPageRequest request, MultipartFile image) throws BusinessException {
        ValidatedImage validatedImage = validateImage(image);
        AboutPageContent content = contentRepository.findById(PAGE_ID)
                .orElseThrow(() -> new SourceNotFound("About page content not found"));
        content.setTitle(request.title().trim());
        content.setIntroduction(request.introduction().trim());
        content.setDescription(request.description().trim());
        content.setAddress(request.address().trim());
        content.setContact(request.contact().trim());
        if (validatedImage != null) {
            content.setImageContentType(validatedImage.contentType());
        }
        AboutPageContent saved = contentRepository.saveAndFlush(content);
        if (validatedImage != null) {
            imageRepository.save(new AboutPageImage(PAGE_ID, validatedImage.data()));
        }
        return saved;
    }

    private ValidatedImage validateImage(MultipartFile image) throws BusinessException {
        if (image == null || image.isEmpty()) return null;
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("Package image must not exceed 5 MB");
        }
        String contentType = image.getContentType();
        if (!"image/jpeg".equalsIgnoreCase(contentType) && !"image/png".equalsIgnoreCase(contentType)) {
            throw new BusinessException("Package image must be a JPEG or PNG file");
        }

        try {
            byte[] data = image.getBytes();
            try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
                if (input == null) throw new BusinessException("Invalid package image");
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new BusinessException("Invalid package image");
                ImageReader reader = readers.next();
                try {
                    reader.setInput(input, true, true);
                    String format = reader.getFormatName();
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    boolean expectedFormat = "image/png".equalsIgnoreCase(contentType)
                            ? "png".equalsIgnoreCase(format)
                            : "jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format);
                    if (!expectedFormat || width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION
                            || (long) width * height > 16_000_000L) {
                        throw new BusinessException("Invalid package image");
                    }
                    BufferedImage decoded = reader.read(0);
                    if (decoded == null) throw new BusinessException("Invalid package image");
                } finally {
                    reader.dispose();
                }
            }
            return new ValidatedImage(data, contentType.toLowerCase());
        } catch (IOException ex) {
            throw new BusinessException("Could not read package image");
        }
    }

    private record ValidatedImage(byte[] data, String contentType) {
    }
}
