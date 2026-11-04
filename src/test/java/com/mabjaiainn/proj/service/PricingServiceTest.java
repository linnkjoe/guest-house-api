package com.mabjaiainn.proj.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mabjaiainn.proj.DTO.QuoteResponse;
import com.mabjaiainn.proj.entity.PackFeature;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.exception.BusinessException;
import com.mabjaiainn.proj.repository.PackFeatureRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

    private PacksRepository packsRepository;
    private PackFeatureRepository featureRepository;
    private PricingService pricingService;
    private Packs basic;
    private PackFeature ac;
    private PackFeature pool;

    @BeforeEach
    void setUp() {
        packsRepository = mock(PacksRepository.class);
        featureRepository = mock(PackFeatureRepository.class);
        pricingService = new PricingService(packsRepository, featureRepository, new BigDecimal("500"));

        ac = feature(1L, "AC", "Ar condicionado", "400");
        pool = feature(2L, "POOL", "Piscina", "300");
        basic = new Packs("Basic", 4, true, new BigDecimal("1000"));
        basic.getFeatures().add(ac); // o pacote mais simples ja inclui ar condicionado
        Packs expensive = new Packs("Summer", 6, true, new BigDecimal("1500"));

        when(packsRepository.findByCustomFalseOrderByPackPriceAscDurationAsc())
                .thenReturn(List.of(basic, expensive));
    }

    @Test
    void baseOnly_costsTheSimplestPack() throws BusinessException {
        QuoteResponse quote = pricingService.quote(4, List.of());

        assertEquals(new BigDecimal("1000.00"), quote.total());
        assertEquals(List.of(1L), quote.featureIds());
    }

    @Test
    void extraNotInBase_isChargedAndIncludedFeatureIsFree() throws BusinessException {
        when(featureRepository.findAllById(any())).thenReturn(List.of(ac, pool));

        QuoteResponse quote = pricingService.quote(4, List.of(1L, 2L));

        assertEquals(new BigDecimal("1300.00"), quote.total()); // 1000 + piscina 300, AC ja incluido
    }

    @Test
    void extraHours_areChargedAtTheHourlyRate() throws BusinessException {
        when(featureRepository.findAllById(any())).thenReturn(List.of(pool));

        QuoteResponse quote = pricingService.quote(6, List.of(2L));

        assertEquals(new BigDecimal("2300.00"), quote.total()); // 1000 + 300 + 2h * 500
    }

    @Test
    void durationShorterThanBase_isRejected() {
        assertThrows(BusinessException.class, () -> pricingService.quote(3, List.of()));
    }

    @Test
    void inactiveFeature_isRejected() {
        pool.setActive(false);
        when(featureRepository.findAllById(any())).thenReturn(List.of(pool));

        assertThrows(BusinessException.class, () -> pricingService.quote(4, List.of(2L)));
    }

    private static PackFeature feature(Long id, String code, String name, String price) {
        PackFeature f = new PackFeature(code, name, null, new BigDecimal(price));
        f.setFeatureId(id);
        return f;
    }
}
