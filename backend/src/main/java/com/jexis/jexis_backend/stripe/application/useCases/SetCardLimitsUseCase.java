package com.jexis.jexis_backend.stripe.application.useCases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.card.domain.enums.SpendingInterval;
import com.jexis.jexis_backend.common.dto.SpendingLimit;
import com.stripe.StripeClient;
import com.stripe.model.issuing.Card;
import com.stripe.net.RequestOptions;
import com.stripe.param.issuing.CardUpdateParams;

@Service
public class SetCardLimitsUseCase {
    private final StripeClient client;

    public SetCardLimitsUseCase(StripeClient client) {
        this.client = client;
    }

    public void execute(String accountId, String cardId, List<SpendingLimit> limits) {
        try {
            RequestOptions cardRequestOptions = RequestOptions.builder()
                    .setStripeAccount(accountId)
                    .build();

            Card resource = client.v1()
                    .issuing()
                    .cards()
                    .retrieve(cardId, cardRequestOptions);

            CardUpdateParams.SpendingControls.Builder spendingControlsBuilder = CardUpdateParams.SpendingControls
                    .builder();

            for (SpendingLimit limit : limits) {
                spendingControlsBuilder.addSpendingLimit(
                        CardUpdateParams.SpendingControls.SpendingLimit.builder()
                                .setAmount(limit.getAmount())
                                .setInterval(mapInterval(limit.getInterval()))
                                .build());
            }

            CardUpdateParams params = CardUpdateParams.builder()
                    .setSpendingControls(spendingControlsBuilder.build())
                    .build();

            RequestOptions requestOptions = RequestOptions.builder().setStripeAccount(accountId).build();

            resource.update(params, requestOptions);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private CardUpdateParams.SpendingControls.SpendingLimit.Interval mapInterval(SpendingInterval interval) {
        return switch (interval) {
            case daily -> CardUpdateParams.SpendingControls.SpendingLimit.Interval.DAILY;
            case weekly -> CardUpdateParams.SpendingControls.SpendingLimit.Interval.WEEKLY;
            case monthly -> CardUpdateParams.SpendingControls.SpendingLimit.Interval.MONTHLY;
            case yearly -> CardUpdateParams.SpendingControls.SpendingLimit.Interval.YEARLY;
            case all_time -> CardUpdateParams.SpendingControls.SpendingLimit.Interval.ALL_TIME;
            case per_authorization -> CardUpdateParams.SpendingControls.SpendingLimit.Interval.PER_AUTHORIZATION;
            default -> throw new IllegalArgumentException("Unsupported interval: " + interval);
        };
    }
}
