package com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute;

import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.application.dto.DisputeReason;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.issuing.Dispute;
import com.stripe.net.RequestOptions;
import com.stripe.param.issuing.DisputeCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateStripeDisputeUseCase {
    private final StripeClient client;
    private final CreateStripeDisputeCanceledUseCase createStripeDisputeCanceledUseCase;
    private final CreateStripeDisputeDuplicateUseCase createStripeDisputeDuplicateUseCase;
    private final CreateStripeDisputeFraudulentUseCase createStripeDisputeFraudulentUseCase;
    private final CreateStripeDisputeMerchandiseNotAsDescribedUseCase createStripeDisputeMerchandiseNotAsDescribedUseCase;
    private final CreateStripeDisputeNoValidAuthorizationUseCase createStripeDisputeNoValidAuthorizationUseCase;
    private final CreateStripeDisputeNotReceivedUseCase createStripeDisputeNotReceivedUseCase;
    private final CreateStripeDisputeOtherUseCase createStripeDisputeOtherUseCase;
    private final CreateStripeDisputeServiceNotAsDescribedUseCase createStripeDisputeServiceNotAsDescribedUseCase;

    public Dispute execute(CreateDisputeDto body, String connectAccountId, String transactionId) {
        try {
            RequestOptions requestOptions =
                    RequestOptions.builder().setStripeAccount(connectAccountId).build();

            DisputeCreateParams.Builder params =
                    DisputeCreateParams.builder();

            params.setAmount(body.amount());
            params.setTransaction(transactionId);

            DisputeCreateParams.Evidence.Builder builder = DisputeCreateParams.Evidence.builder();

            builder.setReason(mapReason(body.disputeEvidence().reason()));

            switch (body.disputeEvidence().reason()) {
                case canceled -> createStripeDisputeCanceledUseCase.execute(body, builder);
                case duplicate -> createStripeDisputeDuplicateUseCase.execute(body, builder);
                case fraudulent -> createStripeDisputeFraudulentUseCase.execute(body, builder);
                case merchandise_not_as_described -> createStripeDisputeMerchandiseNotAsDescribedUseCase.execute(body, builder);
                case no_valid_authorization -> createStripeDisputeNoValidAuthorizationUseCase.execute(body, builder);
                case not_received -> createStripeDisputeNotReceivedUseCase.execute(body, builder);
                case other -> createStripeDisputeOtherUseCase.execute(body, builder);
                case service_not_as_described -> createStripeDisputeServiceNotAsDescribedUseCase.execute(body, builder);
            }

            params.setEvidence(builder.build());

            return client.v1().issuing().disputes().create(params.build(), requestOptions);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe error when creating dispute: ", e);
        }
    }

    private DisputeCreateParams.Evidence.Reason mapReason(DisputeReason reason) {
        return switch (reason) {
            case canceled -> DisputeCreateParams.Evidence.Reason.CANCELED;
            case duplicate -> DisputeCreateParams.Evidence.Reason.DUPLICATE;
            case fraudulent -> DisputeCreateParams.Evidence.Reason.FRAUDULENT;
            case merchandise_not_as_described -> DisputeCreateParams.Evidence.Reason.MERCHANDISE_NOT_AS_DESCRIBED;
            case no_valid_authorization -> DisputeCreateParams.Evidence.Reason.NO_VALID_AUTHORIZATION;
            case not_received -> DisputeCreateParams.Evidence.Reason.NOT_RECEIVED;
            case other -> DisputeCreateParams.Evidence.Reason.OTHER;
            case service_not_as_described -> DisputeCreateParams.Evidence.Reason.SERVICE_NOT_AS_DESCRIBED;
        };
    }
}
