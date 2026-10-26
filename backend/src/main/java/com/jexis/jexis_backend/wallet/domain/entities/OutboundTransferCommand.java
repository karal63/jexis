package com.jexis.jexis_backend.wallet.domain.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbound_transfer_commands", uniqueConstraints = @UniqueConstraint(columnNames = {"wallet_id", "request_key"}))
@Getter
@Setter
public class OutboundTransferCommand {
    @Id private UUID id;
    @Column(nullable = false) private UUID walletId;
    @Column(nullable = false) private UUID requestKey;
    @Column(nullable = false) private UUID externalAccountId;
    @Column(nullable = false) private Long amount;
    @Column(nullable = false) private String currency;
    @Column(nullable = false, columnDefinition = "text") private String description;
    @Column(nullable = false) private String stripeAccountId;
    @Column(nullable = false) private String financialAccountId;
    @Column(nullable = false) private String destinationId;
    @Column(nullable = false) private Instant createdAt;
    private String stripeTransferId;
}
