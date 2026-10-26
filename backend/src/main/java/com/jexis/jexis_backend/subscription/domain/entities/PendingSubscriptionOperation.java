package com.jexis.jexis_backend.subscription.domain.entities;

import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionOperationType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pending_subscription_operations",
        uniqueConstraints = @UniqueConstraint(columnNames = "account_id"))
@Getter
@Setter
public class PendingSubscriptionOperation {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID planId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionOperationType type;

    private String paymentMethodId;

    @Column(columnDefinition = "text")
    private String checkoutUrl;

    private String stripeSubscriptionId;

    @Column(nullable = false)
    private Instant createdAt;
}
