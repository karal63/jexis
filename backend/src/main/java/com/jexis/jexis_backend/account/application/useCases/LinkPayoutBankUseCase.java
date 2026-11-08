package com.jexis.jexis_backend.account.application.useCases;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkPayoutBankUseCase {
    private final GetAccountUseCase accounts;
    private final StripeClient stripe;

    public record Setup(String clientSecret, String name) {}

    public Setup start(UUID accountId) throws StripeException {
        String connectId = accounts.execute(accountId).getConnectAccountId();
        var account = stripe.v1().accounts().retrieve(connectId);

        String name = accountHolderName(account);
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Complete your account name before linking a bank");
        }
        var customers = stripe.v1().customers().search(CustomerSearchParams.builder()
                .setQuery("metadata['payout_account']:'" + connectId + "'").build());
        var customer = customers.getData().isEmpty() ? null : customers.getData().get(0);
        if (customer == null) {
            var customerOptions = RequestOptions.builder()
                    .setIdempotencyKey("payout-customer-" + connectId).build();
            customer = stripe.v1().customers().create(CustomerCreateParams.builder().setName(name)
                    .putMetadata("payout_account", connectId).build(), customerOptions);
        }
        var intent = stripe.v1().setupIntents().create(SetupIntentCreateParams.builder().putAllExtraParam(Map.of(
                "customer", customer.getId(),
                "payment_method_types", List.of("us_bank_account"),
                "flow_directions", List.of("outbound"),
                "metadata", Map.of("payout_account", connectId),
                "payment_method_options", Map.of("us_bank_account", Map.of(
                        "verification_method", "instant",
                        "financial_connections", Map.of("permissions", List.of("payment_method")))))).build());
        return new Setup(intent.getClientSecret(), name);
    }

    static String accountHolderName(com.stripe.model.Account account) {
        String businessType = account.getBusinessType();
        if ("individual".equals(businessType)) {
            var individual = account.getIndividual();
            if (individual == null) return null;
            return java.util.stream.Stream.of(individual.getFirstName(), individual.getLastName())
                    .filter(part -> part != null && !part.isBlank())
                    .collect(java.util.stream.Collectors.joining(" "));
        }
        if ("company".equals(businessType) || "non_profit".equals(businessType)
                || "government_entity".equals(businessType)) {
            return account.getCompany() == null ? null : account.getCompany().getName();
        }
        return null;
    }

    public void finish(UUID accountId, String setupIntentId) throws StripeException {
        String connectId = accounts.execute(accountId).getConnectAccountId();
        var intent = stripe.v1().setupIntents().retrieve(setupIntentId);
        if (!connectId.equals(intent.getMetadata().get("payout_account"))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank setup not found");
        }
        if (!"succeeded".equals(intent.getStatus()) || intent.getCustomer() == null
                || intent.getPaymentMethod() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bank setup is incomplete");
        }
        var options = RequestOptions.builder().setStripeAccount(connectId).build();
        var service = stripe.v1().accounts().externalAccounts();
        for (var external : service.list(connectId, AccountExternalAccountListParams.builder().setObject("bank_account").build(), options).autoPagingIterable()) {
            if (external instanceof com.stripe.model.BankAccount bank
                    && bank.getMetadata() != null
                    && setupIntentId.equals(bank.getMetadata().get("payout_setup"))) return;
        }
        var tokenOptions = RequestOptions.builder().setStripeAccount(connectId)
                .setIdempotencyKey("payout-token-" + setupIntentId).build();
        var token = stripe.v1().tokens().create(TokenCreateParams.builder().putAllExtraParam(Map.of(
                "customer", intent.getCustomer(),
                "bank_account", Map.of("payment_method", intent.getPaymentMethod()))).build(), tokenOptions);
        var attachOptions = RequestOptions.builder().setStripeAccount(connectId)
                .setIdempotencyKey("payout-bank-" + setupIntentId).build();
        service.create(connectId, AccountExternalAccountCreateParams.builder().putAllExtraParam(Map.of(
                "external_account", token.getId(),
                "default_for_currency", true,
                "metadata", Map.of("payout_setup", setupIntentId))).build(), attachOptions);
    }
}
