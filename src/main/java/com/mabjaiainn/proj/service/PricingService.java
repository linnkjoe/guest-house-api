package com.mabjaiainn.proj.service;

import com.mabjaiainn.proj.DTO.QuoteRequest;
import com.mabjaiainn.proj.DTO.QuoteResponse;
import com.mabjaiainn.proj.entity.PackFeature;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.PackFeatureRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Preco de uma estadia personalizada = preco do pacote mais simples (o mais barato)
 * + preco de cada extra que esse pacote ainda nao inclui
 * + horas adicionais ao tempo do pacote base.
 * Os precos vem sempre da configuracao feita pela equipa.
 */
@Service
public class PricingService {

    private static final int MAX_DURATION = 72;

    private final PacksRepository packsRepository;
    private final PackFeatureRepository featureRepository;
    private final BigDecimal extraHourRate;

    public PricingService(PacksRepository packsRepository, PackFeatureRepository featureRepository,
            @Value("${app.pricing.extra-hour-rate:500}") BigDecimal extraHourRate) {
        this.packsRepository = packsRepository;
        this.featureRepository = featureRepository;
        this.extraHourRate = extraHourRate;
    }

    public Packs basePack() throws BusinessException {
        return packsRepository.findByCustomFalseOrderByPackPriceAscDurationAsc().stream()
                .min(Comparator.comparing(Packs::getPackPrice).thenComparing(Packs::getDuration))
                .orElseThrow(() -> new BusinessException("No packs available to personalize"));
    }

    public QuoteResponse quote(QuoteRequest request) throws BusinessException {
        return quote(request.duration(), request.featureIds());
    }

    public BigDecimal extensionPrice(Duration extension) {
        BigDecimal hours = BigDecimal.valueOf(extension.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        return extraHourRate.multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }

    /** Resultado inclui sempre os extras ja presentes no pacote base. */
    public QuoteResponse quote(Integer duration, List<Long> featureIds) throws BusinessException {
        Packs base = basePack();
        if (duration < base.getDuration() || duration > MAX_DURATION) {
            throw new BusinessException("Duration must be between " + base.getDuration() + " and " + MAX_DURATION + " hours");
        }
        Set<PackFeature> chosen = resolveFeatures(featureIds);
        Set<Long> included = new LinkedHashSet<>();
        base.getFeatures().forEach(f -> included.add(f.getFeatureId()));

        List<QuoteResponse.Line> lines = new ArrayList<>();
        BigDecimal basePrice = base.getDiscountedPrice();
        BigDecimal total = basePrice;
        lines.add(new QuoteResponse.Line("Pacote base · " + base.getPackName(), basePrice));

        Set<Long> finalIds = new LinkedHashSet<>(included);
        for (PackFeature feature : chosen) {
            finalIds.add(feature.getFeatureId());
            if (!included.contains(feature.getFeatureId())) {
                lines.add(new QuoteResponse.Line("+ " + feature.getName(), feature.getPrice()));
                total = total.add(feature.getPrice());
            }
        }
        int extraHours = duration - base.getDuration();
        if (extraHours > 0) {
            BigDecimal amount = extraHourRate.multiply(BigDecimal.valueOf(extraHours));
            lines.add(new QuoteResponse.Line("+ " + extraHours + "h adicionais", amount));
            total = total.add(amount);
        }
        return new QuoteResponse(base.getPackId(), base.getPackName(), base.getDuration(), basePrice,
                duration, new ArrayList<>(finalIds), lines, total.setScale(2, RoundingMode.HALF_UP));
    }

    private Set<PackFeature> resolveFeatures(List<Long> ids) throws BusinessException {
        Set<PackFeature> result = new LinkedHashSet<>();
        if (ids == null || ids.isEmpty()) {
            return result;
        }
        Set<Long> wanted = new LinkedHashSet<>(ids);
        for (PackFeature feature : featureRepository.findAllById(wanted)) {
            if (!Boolean.TRUE.equals(feature.getActive())) {
                throw new BusinessException("Extra not available: " + feature.getName());
            }
            result.add(feature);
        }
        if (result.size() != wanted.size()) {
            throw new BusinessException("Feature not found");
        }
        return result;
    }
}
