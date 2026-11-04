package com.mabjaiainn.proj.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Extra que pode fazer parte de uma estadia (ar condicionado, piscina, atendimento VIP...).
 * O preco e o que o cliente paga a mais, face ao pacote mais simples, quando personaliza.
 */
@Entity
@Table(name = "pack_feature")
public class PackFeature {

    /** Codigo da funcionalidade que liga o campo legado Packs.packAc. */
    public static final String CODE_AC = "AC";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pack_feature_seq")
    @SequenceGenerator(name = "pack_feature_seq", sequenceName = "PACK_FEATURE_SEQ", allocationSize = 1)
    private Long featureId;

    @Column(name = "code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public PackFeature() {
    }

    public PackFeature(String code, String name, String description, BigDecimal price) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Long getFeatureId() { return featureId; }
    public void setFeatureId(Long featureId) { this.featureId = featureId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
