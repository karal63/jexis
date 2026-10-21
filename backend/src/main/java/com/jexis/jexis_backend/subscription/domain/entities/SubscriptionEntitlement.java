package com.jexis.jexis_backend.subscription.domain.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscription_entitlements")
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionEntitlement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    @JsonBackReference
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "entitlement_id", nullable = false)
    private Entitlement entitlement;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public SubscriptionEntitlement(Subscription subscription, Entitlement entitlement, String value) {
        this.subscription = subscription;
        this.entitlement = entitlement;
        this.value = value;
    }
}
