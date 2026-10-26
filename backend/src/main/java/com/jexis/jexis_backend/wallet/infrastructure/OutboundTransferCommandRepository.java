package com.jexis.jexis_backend.wallet.infrastructure;

import com.jexis.jexis_backend.wallet.domain.entities.OutboundTransferCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OutboundTransferCommandRepository extends JpaRepository<OutboundTransferCommand, UUID> {
    Optional<OutboundTransferCommand> findByWalletIdAndRequestKey(UUID walletId, UUID requestKey);
}
