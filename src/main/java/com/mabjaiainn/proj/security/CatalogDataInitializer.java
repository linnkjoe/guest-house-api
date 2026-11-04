package com.mabjaiainn.proj.security;

import com.mabjaiainn.proj.entity.PackFeature;
import com.mabjaiainn.proj.entity.Packs;
import com.mabjaiainn.proj.repository.PackFeatureRepository;
import com.mabjaiainn.proj.repository.PacksRepository;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Primeiro arranque com o novo catalogo (tabela de extras vazia):
 * cria os extras base e as estadias "Summer" e "VIP Stay". Depois disso a equipa
 * gere tudo pela aplicacao; nada e recriado se for apagado.
 * Tambem liga o extra "Ar condicionado" as estadias antigas que ja tinham ar condicionado.
 */
@Component
public class CatalogDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogDataInitializer.class);

    private final PackFeatureRepository featureRepository;
    private final PacksRepository packsRepository;

    public CatalogDataInitializer(PackFeatureRepository featureRepository, PacksRepository packsRepository) {
        this.featureRepository = featureRepository;
        this.packsRepository = packsRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        createCheckoutNotificationDemoPack();
        if (featureRepository.count() > 0) {
            return;
        }
        PackFeature ac = featureRepository.save(new PackFeature(PackFeature.CODE_AC, "Ar condicionado",
                "Quarto climatizado durante a estadia", new BigDecimal("400.00")));
        PackFeature pool = featureRepository.save(new PackFeature("POOL", "Piscina",
                "Acesso livre a piscina", new BigDecimal("300.00")));
        PackFeature vip = featureRepository.save(new PackFeature("VIP_SERVICE", "Atendimento VIP bar e restaurante",
                "Atendimento personalizado no bar e no restaurante", new BigDecimal("800.00")));

        for (Packs pack : packsRepository.findByCustomFalseOrderByPackPriceAscDurationAsc()) {
            if (Boolean.TRUE.equals(pack.getPackAc()) && pack.getFeatures().isEmpty()) {
                pack.getFeatures().add(ac);
                packsRepository.save(pack);
            }
        }
        if (!packsRepository.existsByPackNameIgnoreCaseAndCustomFalse("Summer")) {
            packsRepository.save(newPack("Summer", "Ar condicionado e acesso a piscina.",
                    6, "1500.00", Set.of(ac, pool)));
        }
        if (!packsRepository.existsByPackNameIgnoreCaseAndCustomFalse("VIP Stay")) {
            packsRepository.save(newPack("VIP Stay",
                    "Acesso a piscina e atendimento personalizado no bar e no restaurante.",
                    6, "2300.00", Set.of(pool, vip)));
        }
        log.info("Catalogo inicial criado (extras + estadias Summer e VIP Stay). Ajuste os precos na aplicacao.");
    }

    private void createCheckoutNotificationDemoPack() {
        String demoName = "Demo - Aviso de checkout";
        if (packsRepository.existsByPackNameIgnoreCaseAndCustomFalse(demoName)) {
            return;
        }
        Packs demo = newPack(demoName,
                "Pacote de demonstração para testar o aviso de checkout próximo. Duração de 1 hora.",
                1, "100.00", Set.of());
        packsRepository.save(demo);
        log.info("Pacote de demonstração '{}' criado para testes de aviso de checkout.", demoName);
    }

    private Packs newPack(String name, String description, int hours, String price, Set<PackFeature> features) {
        Packs pack = new Packs(name, hours,
                features.stream().anyMatch(f -> PackFeature.CODE_AC.equals(f.getCode())), new BigDecimal(price));
        pack.setDescription(description);
        pack.setCustom(false);
        pack.setFeatures(new LinkedHashSet<>(features));
        return pack;
    }
}
