package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.PackRequest;
import com.mabjaiainn.proj.entity.PackFeature;
import com.mabjaiainn.proj.entity.PackImage;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.repository.PackFeatureRepository;
import com.mabjaiainn.proj.repository.PackImageRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author linnkjoe
 */
@Service
public class PackService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 4096;

    private final PacksRepository packsRepository;
    private final PackFeatureRepository featureRepository;
    private final PackImageRepository packImageRepository;

    public PackService(PacksRepository packsRepository, PackFeatureRepository featureRepository,
            PackImageRepository packImageRepository) {
        this.packsRepository = packsRepository;
        this.featureRepository = featureRepository;
        this.packImageRepository = packImageRepository;
    }

    public Optional<Packs> findPackById(Long id) {
        Optional<Packs> pack = packsRepository.findById(id);
        if (pack.isEmpty()) {
            throw new SourceNotFound("Pack Not Found");
        }
        return pack;
    }

    /** Catalogo de estadias (sem as copias das reservas personalizadas). */
    public List<Packs> retrievePacks() {
        return packsRepository.findByCustomFalseOrderByPackPriceAscDurationAsc();
    }

    @Transactional
    public void deleteById(Long id) throws BusinessException {
        Packs pack = packsRepository.findById(id).orElseThrow(() -> new SourceNotFound("Pack Not Found"));
        try {
            packImageRepository.deleteById(id);
            packImageRepository.flush();
            packsRepository.delete(pack);
            packsRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Pack in use by existing bookings");
        }
    }

    @Transactional
    public Packs createPack(PackRequest request) throws BusinessException {
        return createPack(request, null);
    }

    @Transactional
    public Packs createPack(PackRequest request, MultipartFile image) throws BusinessException {
        ValidatedImage validatedImage = validateImage(image);
        if (packsRepository.existsByPackNameIgnoreCaseAndCustomFalse(request.packName().trim())) {
            throw new BusinessException("Pack name already in use");
        }
        Packs pack = new Packs();
        pack.setCustom(false);
        apply(pack, request);
        applyImageMetadata(pack, validatedImage);
        Packs savedPack = packsRepository.save(pack);
        packsRepository.flush();
        persistImage(savedPack, validatedImage);
        return savedPack;
    }

    @Transactional
    public Packs updatePack(Long id, PackRequest request) throws BusinessException {
        return updatePack(id, request, null);
    }

    @Transactional
    public Packs updatePack(Long id, PackRequest request, MultipartFile image) throws BusinessException {
        ValidatedImage validatedImage = validateImage(image);
        Packs pack = packsRepository.findById(id).orElseThrow(() -> new SourceNotFound("Pack Not Found"));
        if (Boolean.TRUE.equals(pack.getCustom())) {
            throw new BusinessException("Custom copies cannot be edited");
        }
        if (packsRepository.existsByPackNameIgnoreCaseAndCustomFalseAndPackIdNot(request.packName().trim(), id)) {
            throw new BusinessException("Pack name already in use");
        }
        apply(pack, request);
        if (validatedImage != null) {
            applyImageMetadata(pack, validatedImage);
        }
        Packs savedPack = packsRepository.save(pack);
        packsRepository.flush();
        persistImage(savedPack, validatedImage);
        return savedPack;
    }

    @Transactional
    public void recordRequest(Long packId) {
        if (packsRepository.incrementRequestCount(packId) != 1) {
            throw new SourceNotFound("Pack Not Found");
        }
    }

    public Optional<PackImage> findImageByPackId(Long packId) {
        return packImageRepository.findById(packId);
    }

    private void applyImageMetadata(Packs pack, ValidatedImage image) {
        if (image != null) {
            pack.setImageContentType(image.contentType());
        }
    }

    private void persistImage(Packs pack, ValidatedImage image) {
        if (image != null) {
            packImageRepository.save(new PackImage(pack.getPackId(), image.data()));
        }
    }

    private ValidatedImage validateImage(MultipartFile image) throws BusinessException {
        if (image == null || image.isEmpty()) {
            return null;
        }
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
                if (input == null) {
                    throw new BusinessException("Invalid package image");
                }
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) {
                    throw new BusinessException("Invalid package image");
                }
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
                    if (decoded == null) {
                        throw new BusinessException("Invalid package image");
                    }
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

    private void apply(Packs pack, PackRequest request) throws BusinessException {
        Set<PackFeature> features = new LinkedHashSet<>();
        if (request.featureIds() != null && !request.featureIds().isEmpty()) {
            features.addAll(featureRepository.findAllById(request.featureIds()));
            if (features.size() != new LinkedHashSet<>(request.featureIds()).size()) {
                throw new BusinessException("Feature not found");
            }
        }
        boolean hasAc = features.stream().anyMatch(f -> PackFeature.CODE_AC.equals(f.getCode()));
        pack.setPackName(request.packName().trim());
        pack.setDescription(request.description() == null || request.description().isBlank()
                ? null : request.description().trim());
        pack.setDuration(request.duration());
        pack.setPackPrice(request.packPrice().setScale(2, RoundingMode.HALF_UP));
        pack.setPromotionDiscount(request.promotionDiscount() == null
                ? java.math.BigDecimal.ZERO
                : request.promotionDiscount().setScale(2, RoundingMode.HALF_UP));
        pack.setFeatures(features);
        // Com ar condicionado nos extras, o campo legado acompanha; senao respeita o valor enviado.
        pack.setPackAc(hasAc || Boolean.TRUE.equals(request.packAc()));
    }
}
