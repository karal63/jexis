package com.jexis.jexis_backend.invoice.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.invoice.domain.enums.InvoiceStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import com.jexis.jexis_backend.subscription.domain.entities.Subscription;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false, unique = true)
    private String stripeInvoiceId;

    @Column(nullable = false)
    private Long amountPaid;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private String invoicePdf;

    private String hostedInvoiceUrl;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Invoice(Subscription subscription, String stripeInvoiceId, Long amountPaid, String currency, InvoiceStatus status, String invoicePdf, String hostedInvoiceUrl) {
        this.subscription = subscription;
        this.stripeInvoiceId = stripeInvoiceId;
        this.amountPaid = amountPaid;
        this.currency = currency;
        this.status = status;
        this.invoicePdf = invoicePdf;
        this.hostedInvoiceUrl = hostedInvoiceUrl;
    }
}
