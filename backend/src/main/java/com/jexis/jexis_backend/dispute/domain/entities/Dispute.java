package com.jexis.jexis_backend.dispute.domain.entities;

import com.jexis.jexis_backend.dispute.application.dto.DisputeReason;
import com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "disputes")
public class Dispute {

    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    private String stripeDisputeId;

    @Getter
    @Setter
    @OneToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false, unique = true)
    private Transaction transaction;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = false)
    private Wallet wallet;

    @Getter
    @Setter
    @Column(nullable = false)
    private Long amount;

    @Getter
    @Setter
    @Column(nullable = false)
    private String currency;

    @Getter
    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DisputeStatus status;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private DisputeReason reason;

    @Getter
    @Setter
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Getter
    @Setter
    private LocalDateTime resolvedAt;

    public Dispute() {
    }

    public Dispute(String stripeDisputeId, Transaction transaction, Wallet wallet, Long amount, String currency, DisputeStatus status, DisputeReason reason) {
        this.stripeDisputeId = stripeDisputeId;
        this.transaction = transaction;
        this.wallet = wallet;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.reason = reason;
    }
}