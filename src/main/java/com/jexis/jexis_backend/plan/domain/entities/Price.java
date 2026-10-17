package com.jexis.jexis_backend.plan.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "prices")
@Getter
@Setter
@NoArgsConstructor
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    @JsonIgnore
    private Plan plan;

    @Column(unique = true)
    private String stripePriceId;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private Long unitAmount;

    @Column(nullable = false)
    private String interval;

    @Column(nullable = false)
    private Integer intervalCount = 1;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Price(Plan plan, String stripePriceId, String currency, Long unitAmount, String interval, Integer intervalCount, boolean active) {
        this.plan = plan;
        this.stripePriceId = stripePriceId;
        this.currency = currency;
        this.unitAmount = unitAmount;
        this.interval = interval;
        this.intervalCount = intervalCount;
        this.active = active;
    }
}
