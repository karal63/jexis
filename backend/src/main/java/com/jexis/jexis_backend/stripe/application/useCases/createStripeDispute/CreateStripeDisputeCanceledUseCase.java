package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.canceled.CanceledEvidenceDto;
import com.jexis.jexis_backend.stripe.application.helpers.DisputeHelper;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeCanceledUseCase {
    private final DisputeHelper disputeHelper;

    public void execute(CreateDisputeDto body, DisputeCreateParams.Evidence.Builder builder) {
        CanceledEvidenceDto evidence = body.disputeEvidence().canceled();

        builder.setCanceled(
                DisputeCreateParams.Evidence.Canceled.builder()
                        .setProductType(disputeHelper.mapProductType(evidence.productType()))
                        .setProductDescription(evidence.productDescription())
                        .setExpectedAt(evidence.expectedAt())
                        .setReturnStatus(disputeHelper.mapReturnStatus(evidence.returnStatus()))
                        .setReturnedAt(evidence.returnedAt())
                        .setCancellationPolicyProvided(evidence.cancellationPolicyProvided())
                        .setCanceledAt(evidence.canceledAt())
                        .setCancellationReason(evidence.cancellationReason())
                        .setExplanation(evidence.explanation() == null ? null : evidence.explanation())
                        .setAdditionalDocumentation(evidence.additionalDocumentation() == null ? null : evidence.additionalDocumentation())
                        .build()
        );
    }
}
