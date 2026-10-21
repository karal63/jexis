package com.jexis.jexis_backend.stripe.application.useCases;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.card.domain.enums.SpendingInterval;
import com.jexis.jexis_backend.cardholder.application.dto.EditCardHolderDto;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;
import com.jexis.jexis_backend.common.dto.SpendingLimit;
import com.stripe.StripeClient;
import com.stripe.model.issuing.Cardholder;
import com.stripe.net.RequestOptions;
import com.stripe.param.issuing.CardholderUpdateParams;

@Service
public class UpdateStripeCardHolderUseCase {
    private final StripeClient client;

    public UpdateStripeCardHolderUseCase(StripeClient client) {
        this.client = client;
    }

    public void execute(String accountId, String cardHolderId, EditCardHolderDto dto) {
        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setStripeAccount(accountId)
                    .build();

            Cardholder resource = client.v1()
                    .issuing()
                    .cardholders()
                    .retrieve(cardHolderId, requestOptions);

            CardholderUpdateParams.Builder builder = CardholderUpdateParams.builder();

            if (dto.billingAddress() != null) {
                CardholderUpdateParams.Billing.Address.Builder address = CardholderUpdateParams.Billing.Address
                        .builder();

                address.setLine1(
                        dto.billingAddress().line1() != null
                                ? dto.billingAddress().line1()
                                : resource.getBilling().getAddress().getLine1());

                address.setCity(
                        dto.billingAddress().city() != null
                                ? dto.billingAddress().city()
                                : resource.getBilling().getAddress().getCity());

                address.setState(
                        dto.billingAddress().state() != null
                                ? dto.billingAddress().state()
                                : resource.getBilling().getAddress().getState());

                address.setPostalCode(
                        dto.billingAddress().postalCode() != null
                                ? dto.billingAddress().postalCode()
                                : resource.getBilling().getAddress().getPostalCode());

                address.setCountry(
                        dto.billingAddress().country() != null
                                ? dto.billingAddress().country()
                                : resource.getBilling().getAddress().getCountry());

                address.setLine2(dto.billingAddress().line2() != null
                        ? dto.billingAddress().line2()
                        : resource.getBilling().getAddress().getLine2());

                builder.setBilling(
                        CardholderUpdateParams.Billing.builder()
                                .setAddress(address.build())
                                .build());
            }

            if (dto.status() != null) {
                builder.setStatus(getStatus(dto.status()));
            }

            if (dto.spendingControls() != null &&
                    dto.spendingControls().spendingLimits() != null) {

                CardholderUpdateParams.SpendingControls.Builder spendingControlsBuilder = CardholderUpdateParams.SpendingControls
                        .builder();

                for (SpendingLimit limit : dto.spendingControls().spendingLimits()) {
                    spendingControlsBuilder.addSpendingLimit(
                            CardholderUpdateParams.SpendingControls.SpendingLimit.builder()
                                    .setAmount(limit.getAmount())
                                    .setInterval(mapInterval(limit.getInterval()))
                                    .build());
                }

                builder.setSpendingControls(spendingControlsBuilder.build());
            }

            resource.update(builder.build(), requestOptions);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private CardholderUpdateParams.Status getStatus(CardHolderStatus status) {
        return switch (status) {
            case active -> CardholderUpdateParams.Status.ACTIVE;
            case inactive -> CardholderUpdateParams.Status.INACTIVE;
            default -> throw new IllegalArgumentException("Unsupported card holder status: " + status);
        };
    }

    private CardholderUpdateParams.SpendingControls.SpendingLimit.Interval mapInterval(SpendingInterval interval) {
        return switch (interval) {
            case daily -> CardholderUpdateParams.SpendingControls.SpendingLimit.Interval.DAILY;
            case weekly -> CardholderUpdateParams.SpendingControls.SpendingLimit.Interval.WEEKLY;
            case monthly -> CardholderUpdateParams.SpendingControls.SpendingLimit.Interval.MONTHLY;
            case yearly -> CardholderUpdateParams.SpendingControls.SpendingLimit.Interval.YEARLY;
            case all_time -> CardholderUpdateParams.SpendingControls.SpendingLimit.Interval.ALL_TIME;
            case per_authorization -> CardholderUpdateParams.SpendingControls.SpendingLimit.Interval.PER_AUTHORIZATION;
            default -> throw new IllegalArgumentException("Unsupported interval: " + interval);
        };
    }

}
