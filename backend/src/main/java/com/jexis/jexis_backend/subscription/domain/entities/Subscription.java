package com.jexis.jexis_backend.subscription.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionChangeType;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.subscription.domain.exceptions.PlansNotComparableException;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.user.domain.entities.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "price_id", nullable = false)
    private Price price;

    @Column(unique = true)
    private String stripeSubscriptionId;

    private String stripePaymentMethodId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime currentPeriodStart;

    // Null means a local subscription has no expiration.
    private LocalDateTime currentPeriodEnd;

    @Column(nullable = false)
    private boolean cancelAtPeriodEnd = false;

    private LocalDateTime canceledAt;

    @ManyToOne
    @JoinColumn(name = "scheduled_plan_id")
    private Plan scheduledPlan;

    private String stripeScheduleId;

    @OneToMany(
            mappedBy = "subscription",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<SubscriptionEntitlement> entitlements = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Subscription(User user, Account account, Plan plan, Price price, String stripeSubscriptionId, SubscriptionStatus status, LocalDateTime currentPeriodStart, LocalDateTime currentPeriodEnd, boolean cancelAtPeriodEnd) {
        this.user = user;
        this.account = account;
        this.plan = plan;
        this.price = price;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.status = status;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }

    public SubscriptionChangeType determineChange(Plan newPlan) {
        Price newPrice = newPlan.getDefaultPrice();
        if (price == null || newPrice == null
                || price.getCurrency() == null || newPrice.getCurrency() == null
                || !price.getCurrency().equalsIgnoreCase(newPrice.getCurrency())
                || price.getIntervalCount() == null || price.getIntervalCount() <= 0
                || newPrice.getIntervalCount() == null || newPrice.getIntervalCount() <= 0) {
            throw new PlansNotComparableException();
        }

        long currentMultiplier = annualIntervalMultiplier(price.getInterval());
        long newMultiplier = annualIntervalMultiplier(newPrice.getInterval());
        java.math.BigInteger currentCost = java.math.BigInteger.valueOf(price.getUnitAmount())
                .multiply(java.math.BigInteger.valueOf(currentMultiplier))
                .multiply(java.math.BigInteger.valueOf(newPrice.getIntervalCount()));
        java.math.BigInteger newCost = java.math.BigInteger.valueOf(newPrice.getUnitAmount())
                .multiply(java.math.BigInteger.valueOf(newMultiplier))
                .multiply(java.math.BigInteger.valueOf(price.getIntervalCount()));

        int comparison = currentCost.compareTo(newCost);
        if (comparison < 0) {
            return SubscriptionChangeType.UPGRADE;
        } else if (comparison > 0) {
            return SubscriptionChangeType.DOWNGRADE;
        }
        throw new PlansNotComparableException();
    }

    private long annualIntervalMultiplier(String interval) {
        if (interval == null) {
            throw new PlansNotComparableException();
        }
        return switch (interval.toLowerCase(java.util.Locale.ROOT)) {
            case "day" -> 365L;
            case "week" -> 52L;
            case "month" -> 12L;
            case "year" -> 1L;
            default -> throw new PlansNotComparableException();
        };
    }
}
