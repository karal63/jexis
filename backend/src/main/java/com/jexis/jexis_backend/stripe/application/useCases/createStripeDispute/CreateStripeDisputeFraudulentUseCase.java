package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.fraudulent.FraudulentEvidenceDto;
import com.jexis.jexis_backend.stripe.application.helpers.DisputeHelper;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeFraudulentUseCase {
    public void execute(CreateDisputeDto body, DisputeCreateParams.Evidence.Builder builder) {
        FraudulentEvidenceDto evidence = body.disputeEvidence().fraudulent();

        builder.setFraudulent(
                DisputeCreateParams.Evidence.Fraudulent.builder()
                        .setExplanation(evidence.explanation())
                        .setAdditionalDocumentation(evidence.additionalDocumentation() == null ? null : evidence.additionalDocumentation())
                        .build()
        );
    }
}

