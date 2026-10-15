package com.jexis.jexis_backend.plan.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
public class Plan {
    public static final String FREE_CODE = "free";

    @JsonIgnore
    public boolean isFreePlan() {
        return FREE_CODE.equals(code);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String stripePlanId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column
    private PlanStatus status = PlanStatus.DRAFT;

    @OneToMany(
            mappedBy = "plan",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<PlanEntitlement> entitlements = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_price_id", referencedColumnName = "id")
    private Price defaultPrice;

    public Plan(String stripePlanId, String name, String code, String description, boolean active, PlanStatus status) {
        this.stripePlanId = stripePlanId;
        this.name = name;
        this.code = code;
        this.description = description;
        this.active = active;
        this.status = status;
    }

    public boolean isPublishable() {
        return this.defaultPrice != null;
    }

    public boolean isPubliclyAvailable() {
        return this.active && this.defaultPrice != null && (this.status == PlanStatus.PUBLISHED || this.status == null);
    }
}
