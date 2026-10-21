package com.jexis.jexis_backend.dispute.application.useCases;

import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.dispute.domain.exceptions.DisputeNotFoundException;
import com.jexis.jexis_backend.dispute.infrastructure.DisputeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetDisputeUseCase {
    private final DisputeRepository disputeRepository;

    public GetDisputeUseCase(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public Dispute execute(UUID id) {
        return disputeRepository.findById(id)
                .orElseThrow(DisputeNotFoundException::new);
    }
}
