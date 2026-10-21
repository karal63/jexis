package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.notAsDescribed.MerchandiseNotAsDescribedDto;
import com.jexis.jexis_backend.stripe.application.helpers.DisputeHelper;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeMerchandiseNotAsDescribedUseCase {
    private final DisputeHelper disputeHelper;

    public void execute(CreateDisputeDto body, DisputeCreateParams.Evidence.Builder builder) {
        MerchandiseNotAsDescribedDto evidence = body.disputeEvidence().merchandiseNotAsDescribed();

        builder.setMerchandiseNotAsDescribed(
                DisputeCreateParams.Evidence.MerchandiseNotAsDescribed.builder()
                        .setAdditionalDocumentation(evidence.additionalDocumentation() == null ? null : evidence.additionalDocumentation())
                        .setExplanation(evidence.explanation())
                        .setReceivedAt(evidence.receivedAt())
                        .setReturnDescription(evidence.returnDescription() == null ? null : evidence.returnDescription())
                        .setReturnStatus(disputeHelper.mapMerchandiseReturnStatus(evidence.returnStatus()))
                        .setReturnedAt(evidence.returnedAt())
                        .build()
        );
    }
}
