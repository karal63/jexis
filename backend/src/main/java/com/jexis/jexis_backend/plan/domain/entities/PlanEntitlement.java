package com.jexis.jexis_backend.plan.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.CreationTimestamp;

import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plan_entitlements")
@Getter
@Setter
@NoArgsConstructor
public class PlanEntitlement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "entitlement_id", nullable = false)
    private Entitlement entitlement;

    @Column(nullable = false)
    private String value; // Store boolean or number as string

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public PlanEntitlement(Plan plan, Entitlement entitlement, String value) {
        this.plan = plan;
        this.entitlement = entitlement;
        this.value = value;
    }
}
