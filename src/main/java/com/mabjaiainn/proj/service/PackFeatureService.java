package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.FeatureRequest;
import com.mabjaiainn.proj.entity.PackFeature;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.exception.SourceNotFound;
import com.mabjaiainn.proj.repository.PackFeatureRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PackFeatureService {

    private final PackFeatureRepository repository;

    public PackFeatureService(PackFeatureRepository repository) {
        this.repository = repository;
    }

    /** Clientes so veem os extras ativos; a equipa ve todos (para poder reativar). */
    public List<PackFeature> list(boolean onlyActive) {
        return onlyActive ? repository.findByActiveTrueOrderByNameAsc() : repository.findAllByOrderByNameAsc();
    }

    @Transactional
    public PackFeature create(FeatureRequest request) {
        PackFeature feature = new PackFeature();
        feature.setCode(uniqueCode(request.name()));
        apply(feature, request);
        return repository.save(feature);
    }

    @Transactional
    public PackFeature update(Long id, FeatureRequest request) {
        PackFeature feature = repository.findById(id).orElseThrow(() -> new SourceNotFound("Feature Not Found"));
        apply(feature, request);
        return repository.save(feature);
    }

    @Transactional
    public void delete(Long id) throws BusinessException {
        PackFeature feature = repository.findById(id).orElseThrow(() -> new SourceNotFound("Feature Not Found"));
        try {
            repository.delete(feature);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException("Extra in use, deactivate it instead of removing it");
        }
    }

    private void apply(PackFeature feature, FeatureRequest request) {
        feature.setName(request.name().trim());
        feature.setDescription(request.description() == null || request.description().isBlank()
                ? null : request.description().trim());
        feature.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        feature.setActive(request.active() == null || request.active());
    }

    private String uniqueCode(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^A-Za-z0-9]+", "_").toUpperCase();
        base = base.replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "EXTRA";
        base = base.length() > 30 ? base.substring(0, 30) : base;
        String code = base;
        int n = 2;
        while (repository.existsByCode(code)) {
            code = base + "_" + n++;
        }
        return code;
    }
}
