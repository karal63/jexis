package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.notReceived.NotReceivedDto;
import com.jexis.jexis_backend.stripe.application.helpers.DisputeHelper;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeNotReceivedUseCase {
    private final DisputeHelper disputeHelper;

    public void execute(CreateDisputeDto body, DisputeCreateParams.Evidence.Builder builder) {
        NotReceivedDto evidence = body.disputeEvidence().notReceived();

        builder.setNotReceived(
                DisputeCreateParams.Evidence.NotReceived.builder()
                        .setAdditionalDocumentation(evidence.additionalDocumentation() == null ? null : evidence.additionalDocumentation())
                        .setExpectedAt(evidence.expectedAt())
                        .setExplanation(evidence.explanation())
                        .setProductDescription(evidence.productDescription())
                        .setProductType(disputeHelper.mapNotReceivedProductType(evidence.productType()))
                        .build()
        );
    }
}
