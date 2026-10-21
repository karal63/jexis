package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.duplicate.DuplicateEvidenceDto;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeDuplicateUseCase {
    public void execute(CreateDisputeDto body, DisputeCreateParams.Evidence.Builder builder) {
        DuplicateEvidenceDto evidence = body.disputeEvidence().duplicate();

        builder.setDuplicate(
                DisputeCreateParams.Evidence.Duplicate.builder()
                        .setAdditionalDocumentation(evidence.additionalDocumentation() == null ? null : evidence.additionalDocumentation())
                        .setCardStatement(evidence.cardStatement() == null ? null : evidence.cardStatement())
                        .setCashReceipt(evidence.cashReceipt() == null ? null : evidence.cashReceipt())
                        .setCheckImage(evidence.checkImage() == null ? null : evidence.checkImage())
                        .setExplanation(evidence.explanation())
                        .setOriginalTransaction(evidence.originalTransaction() == null ? null : evidence.originalTransaction())
                        .build()
        );
    }
}
