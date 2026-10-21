package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.other.OtherEvidenceDto;
import com.jexis.jexis_backend.stripe.application.helpers.DisputeHelper;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeOtherUseCase {
    private final DisputeHelper disputeHelper;

    public void execute(CreateDisputeDto body, DisputeCreateParams.Evidence.Builder builder) {
        OtherEvidenceDto evidence = body.disputeEvidence().other();

        builder.setOther(
                DisputeCreateParams.Evidence.Other.builder()
                        .setAdditionalDocumentation(evidence.additionalDocumentation() == null ? null : evidence.additionalDocumentation())
                        .setExplanation(evidence.explanation())
                        .setProductDescription(evidence.productDescription())
                        .setProductType(disputeHelper.mapOtherProductType(evidence.productType()))
                        .build()
        );
    }
}
