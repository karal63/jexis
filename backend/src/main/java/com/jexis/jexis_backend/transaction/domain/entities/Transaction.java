package com.jexis.jexis_backend.transaction.domain.entities;

import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.transaction.domain.enums.PaymentMethod;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = false)
    private Wallet wallet;

    @Getter
    @Setter
    @Column(unique = true)
    private String stripeObjectId;

    @Getter
    @Setter
    @Column()
    private String stripeTransactionId;

    @Getter
    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

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
    private TransactionStatus status;

    @Getter
    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionDirection direction;

    // bank payments
    @Getter
    @Setter
    @Column
    private String bankName;

    @Getter
    @Setter
    @Column
    private String bankAccountLast4;

    @Getter
    @Setter
    @Column
    private String routingNumber;

    @Getter
    @Setter
    @Column
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Getter
    @Setter
    @Column
    private LocalDateTime expectedArrivalDate;

    // card payments
    /** Original signed amount in merchantCurrency minor units, as reported by Stripe. */
    @Getter
    @Setter
    @Column
    private Long merchantAmount;

    @Getter
    @Setter
    @Column(length = 3)
    private String merchantCurrency;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "card_id", referencedColumnName = "id")
    private Card card;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "authorization_id", referencedColumnName = "id")
    private Authorization authorization;

    @Getter
    @Setter
    @Column
    private String merchantName;

    @Getter
    @Setter
    @Column
    private String merchantCategory;

    @Getter
    @Setter
    @Column
    private String merchantCity;

    @Getter
    @Setter
    @Column
    private String merchantCountry;

    @Getter
    @Setter
    @OneToOne(mappedBy = "transaction")
    private Dispute dispute;

    @Getter
    @Setter
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(
            Wallet wallet,
            String stripeTransactionId,
            String stripeObjectId,
            TransactionType type,
            Long amount,
            String currency,
            TransactionStatus status,
            TransactionDirection direction) {
        this.wallet = wallet;
        this.stripeTransactionId = stripeTransactionId;
        this.stripeObjectId = stripeObjectId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.direction = direction;
    }
}