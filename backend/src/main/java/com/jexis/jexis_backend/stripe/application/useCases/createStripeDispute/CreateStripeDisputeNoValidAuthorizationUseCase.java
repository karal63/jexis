package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.noValidAuthorization.NoValidAuthorizationDto;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeNoValidAuthorizationUseCase {
    public void execute(CreateDisputeDto body, DisputeCreateParams.Evidence.Builder builder) {
        NoValidAuthorizationDto evidence = body.disputeEvidence().noValidAuthorization();

        builder.setNoValidAuthorization(
                DisputeCreateParams.Evidence.NoValidAuthorization.builder()
                        .setAdditionalDocumentation(evidence.additionalDocumentation() == null ? null : evidence.additionalDocumentation())
                        .setExplanation(evidence.explanation())
                        .build()
        );
    }
}
