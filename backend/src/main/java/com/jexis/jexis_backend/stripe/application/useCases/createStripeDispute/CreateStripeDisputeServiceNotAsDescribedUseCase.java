package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.notAsDescribed.ServiceNotAsDescribedDto;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeServiceNotAsDescribedUseCase {
    public void execute(CreateDisputeDto body, DisputeCreateParams.Evidence.Builder builder) {
        ServiceNotAsDescribedDto evidence = body.disputeEvidence().serviceNotAsDescribed();

        builder.setServiceNotAsDescribed(
                DisputeCreateParams.Evidence.ServiceNotAsDescribed.builder()
                        .setAdditionalDocumentation(evidence.additionalDocumentation() == null ? null :evidence.additionalDocumentation())
                        .setCanceledAt(evidence.canceledAt())
                        .setCancellationReason(evidence.cancellationReason())
                        .setExplanation(evidence.explanation())
                        .setReceivedAt(evidence.receivedAt())
                        .build()
        );
    }
}
