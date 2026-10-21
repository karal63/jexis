package com.jexis.jexis_backend.entitlement.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.subscription.domain.entities.SubscriptionEntitlement;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "entitlements")
@Getter
@Setter
@NoArgsConstructor
public class Entitlement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String type;

    private String description;

    @OneToMany(
            mappedBy = "entitlement",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<PlanEntitlement> planEntitlements = new ArrayList<>();

    @OneToMany(
            mappedBy = "entitlement",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<SubscriptionEntitlement> subscriptionEntitlements = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Entitlement(String key, String type, String description) {
        this.key = key;
        this.type = type;
        this.description = description;
    }
}
