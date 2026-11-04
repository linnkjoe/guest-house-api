package com.mabjaiainn.proj.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/** Estadia personalizada guardada no historico do cliente, com nome dado por ele. */
@Entity
@Table(name = "custom_package")
public class CustomPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "custom_package_seq")
    @SequenceGenerator(name = "custom_package_seq", sequenceName = "CUSTOM_PACKAGE_SEQ", allocationSize = 1)
    private Long customId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "custom_package_feature",
            joinColumns = @JoinColumn(name = "custom_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id"))
    private Set<PackFeature> features = new LinkedHashSet<>();

    @Column(name = "last_price")
    private BigDecimal lastPrice;

    @Column(name = "use_count", nullable = false)
    private Integer useCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public Long getCustomId() { return customId; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Set<PackFeature> getFeatures() { return features; }
    public void setFeatures(Set<PackFeature> features) { this.features = features; }
    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
    public Integer getUseCount() { return useCount; }
    public void setUseCount(Integer useCount) { this.useCount = useCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
