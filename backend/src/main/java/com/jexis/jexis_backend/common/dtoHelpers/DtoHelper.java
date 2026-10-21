package com.jexis.jexis_backend.common.dtoHelpers;

import com.jexis.jexis_backend.plan.application.dto.PlanResponseDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.subscription.application.dto.SubscriptionResponseDto;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.user.application.dto.PaymentMethodResponseDto;
import com.jexis.jexis_backend.user.application.useCases.GetPaymentMethodUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.account.application.dto.AccountAdminResponseDto;
import com.jexis.jexis_backend.account.application.dto.AccountResponseDto;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.authorization.application.dto.AuthorizationAdminResponseDto;
import com.jexis.jexis_backend.authorization.application.dto.AuthorizationResponseDto;
import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.card.application.dto.CardAdminResponseDto;
import com.jexis.jexis_backend.card.application.dto.CardResponseDto;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.cardholder.application.dto.CardHolderAdminResponseDto;
import com.jexis.jexis_backend.cardholder.application.dto.CardHolderResponseDto;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.dispute.application.dto.DisputeAdminResponseDto;
import com.jexis.jexis_backend.dispute.application.dto.DisputeResponseDto;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.externalAccount.application.dto.ExternalAccountAdminResponseDto;
import com.jexis.jexis_backend.externalAccount.application.dto.ExternalAccountResponseDto;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.member.application.dto.MemberAdminResponseDto;
import com.jexis.jexis_backend.member.application.dto.MemberResponseDto;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.transaction.application.dto.TransactionAdminResponseDto;
import com.jexis.jexis_backend.transaction.application.dto.TransactionResponseDto;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.user.application.dto.UserAdminResponseDto;
import com.jexis.jexis_backend.user.application.dto.UserResponseDto;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.wallet.application.dto.WalletAdminResponseDto;
import com.jexis.jexis_backend.wallet.application.dto.WalletResponseDto;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;

@Service
@Slf4j
public class DtoHelper {
    private final GetPaymentMethodUseCase getPaymentMethodUseCase;

    public DtoHelper(GetPaymentMethodUseCase getPaymentMethodUseCase) {
        this.getPaymentMethodUseCase = getPaymentMethodUseCase;
    }

    public UserResponseDto toUserDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRoles(),
                user.getIsActivated(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public AccountResponseDto toAccountDto(Account account) {
        return new AccountResponseDto(
                account.getId(),
                account.getFirstName(),
                account.getLastName(),
                account.getCity(),
                account.getCountry(),
                account.getLine1(),
                account.getLine2(),
                account.getPostalCode(),
                account.getState(),
                account.getPhone(),
                account.getEmail(),
                account.getConnectAccountId(),
                account.getAccountLink(),
                toUserDto(account.getOwner()),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    public CardHolderResponseDto toCardHolderDto(CardHolder cardHolder) {
        return new CardHolderResponseDto(
                cardHolder.getId(),
                toAccountDto(cardHolder.getAccount()),
                toUserDto(cardHolder.getUser()),
                cardHolder.getName(),
                cardHolder.getBillingAddressLine1(),
                cardHolder.getBillingAddressLine2(),
                cardHolder.getBillingCity(),
                cardHolder.getBillingState(),
                cardHolder.getBillingCountry(),
                cardHolder.getBillingPostalCode(),
                cardHolder.getSpendingLimits(),
                cardHolder.getStatus(),
                cardHolder.getCreatedAt(),
                cardHolder.getUpdatedAt());
    }

    public WalletResponseDto toWalletDto(Wallet wallet) {
        return new WalletResponseDto(
                wallet.getId(),
                wallet.getName(),
                toAccountDto(wallet.getAccount()),
                wallet.getAvailableBalance(),
                wallet.getCurrency(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt());
    }

    public CardResponseDto toCardDto(Card card) {
        return new CardResponseDto(
                card.getId(),
                toCardHolderDto(card.getCardHolder()),
                toWalletDto(card.getTreasuryAccount()),
                toUserDto(card.getUser()),
                card.getLast4(),
                card.getStatus(),
                card.getSpendingLimits(),
                card.getBrand(),
                card.getType(),
                card.getCurrency(),
                card.getExpYear(),
                card.getCreatedAt());
    }

    public MemberResponseDto toMemberDto(Member member) {
        return new MemberResponseDto(
                member.getId(),
                toAccountDto(member.getAccount()),
                toUserDto(member.getUser()),
                member.getRole());
    }

    public TransactionResponseDto toTransactionDto(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                toWalletDto(transaction.getWallet()),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantAmount(),
                transaction.getMerchantCurrency(),
                transaction.getStatus(),
                transaction.getDirection(),
                transaction.getBankName(),
                transaction.getBankAccountLast4(),
                transaction.getRoutingNumber(),
                transaction.getPaymentMethod(),
                transaction.getCard() != null ? toCardDto(transaction.getCard()) : null,
                transaction.getMerchantName(),
                transaction.getMerchantCategory(),
                transaction.getMerchantCity(),
                transaction.getMerchantCountry(),
                transaction.getCreatedAt());
    }

    public AuthorizationResponseDto toAuthorizationDto(Authorization authorization) {
        return new AuthorizationResponseDto(
                authorization.getId(),
                toWalletDto(authorization.getWallet()),
                toCardDto(authorization.getCard()),
                authorization.getApproved(),
                authorization.getAmount(),
                authorization.getCurrency(),
                authorization.getStatus(),
                authorization.getMerchantName(),
                authorization.getMerchantCategory(),
                authorization.getMerchantCity(),
                authorization.getMerchantCountry(),
                authorization.getCreatedAt());
    }

    public DisputeResponseDto toDisputeDto(Dispute dispute) {
        return new DisputeResponseDto(
                dispute.getId(),
                toTransactionDto(dispute.getTransaction()),
                toWalletDto(dispute.getWallet()),
                dispute.getAmount(),
                dispute.getCurrency(),
                dispute.getStatus(),
                dispute.getReason(),
                dispute.getCreatedAt(),
                dispute.getResolvedAt());
    }

    public ExternalAccountResponseDto toExternalAccountDto(ExternalAccount externalAccount) {
        return new ExternalAccountResponseDto(
                externalAccount.getId(),
                toAccountDto(externalAccount.getAccount()),
                externalAccount.getBankName(),
                externalAccount.getLast4(),
                externalAccount.getCountry(),
                externalAccount.getCurrency(),
                externalAccount.isDefault(),
                externalAccount.getCreatedAt());
    }

    // Admin methods with all properties
    public UserAdminResponseDto toUserAdminDto(User user) {
        return new UserAdminResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRoles(),
                user.getIsActivated(),
                user.getIsDeleted(),
                user.getDeletedAt(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public AccountAdminResponseDto toAccountAdminDto(Account account) {
        return new AccountAdminResponseDto(
                account.getId(),
                account.getFirstName(),
                account.getLastName(),
                account.getCity(),
                account.getCountry(),
                account.getLine1(),
                account.getLine2(),
                account.getPostalCode(),
                account.getState(),
                account.getPhone(),
                account.getEmail(),
                account.getConnectAccountId(),
                account.getAccountLink(),
                toUserAdminDto(account.getOwner()),
                account.getIsDeleted(),
                account.getDeletedAt(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    public CardHolderAdminResponseDto toCardHolderAdminDto(CardHolder cardHolder) {
        return new CardHolderAdminResponseDto(
                cardHolder.getId(),
                cardHolder.getStripeCardHolderId(),
                toAccountAdminDto(cardHolder.getAccount()),
                toUserAdminDto(cardHolder.getUser()),
                cardHolder.getName(),
                cardHolder.getBillingAddressLine1(),
                cardHolder.getBillingAddressLine2(),
                cardHolder.getBillingCity(),
                cardHolder.getBillingState(),
                cardHolder.getBillingCountry(),
                cardHolder.getBillingPostalCode(),
                cardHolder.getSpendingLimits(),
                cardHolder.getStatus().toString(),
                cardHolder.getIsDeleted(),
                cardHolder.getDeletedAt(),
                cardHolder.getCreatedAt(),
                cardHolder.getUpdatedAt());
    }

    public WalletAdminResponseDto toWalletAdminDto(Wallet wallet) {
        return new WalletAdminResponseDto(
                wallet.getId(),
                wallet.getName(),
                wallet.getStripeFinancialAccountId(),
                toAccountAdminDto(wallet.getAccount()),
                wallet.getAvailableBalance(),
                wallet.getCurrency(),
                wallet.getIsDeleted(),
                wallet.getDeletedAt(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt());
    }

    public CardAdminResponseDto toCardAdminDto(Card card) {
        return new CardAdminResponseDto(
                card.getId(),
                card.getStripeCardId(),
                toCardHolderAdminDto(card.getCardHolder()),
                toWalletAdminDto(card.getTreasuryAccount()),
                toUserAdminDto(card.getUser()),
                card.getLast4(),
                card.getStatus().toString(),
                card.getSpendingLimits(),
                card.getBrand(),
                card.getType(),
                card.getCurrency(),
                card.getExpYear(),
                card.getIsDeleted(),
                card.getDeletedAt(),
                card.getCreatedAt(),
                card.getUpdatedAt());
    }

    public MemberAdminResponseDto toMemberAdminDto(Member member) {
        return new MemberAdminResponseDto(
                member.getId(),
                toAccountAdminDto(member.getAccount()),
                toUserAdminDto(member.getUser()),
                member.getRole().toString());
    }

    public TransactionAdminResponseDto toTransactionAdminDto(Transaction transaction) {
        return new TransactionAdminResponseDto(
                transaction.getId(),
                toWalletAdminDto(transaction.getWallet()),
                transaction.getStripeTransactionId(),
                transaction.getStripeObjectId(),
                transaction.getType().toString(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getMerchantAmount(),
                transaction.getMerchantCurrency(),
                transaction.getStatus().toString(),
                transaction.getDirection().toString(),
                transaction.getBankName(),
                transaction.getBankAccountLast4(),
                transaction.getRoutingNumber(),
                transaction.getPaymentMethod(),
                transaction.getCard() != null ? toCardAdminDto(transaction.getCard()) : null,
                transaction.getMerchantName(),
                transaction.getMerchantCategory(),
                transaction.getMerchantCity(),
                transaction.getMerchantCountry(),
                transaction.getCreatedAt());
    }

    public AuthorizationAdminResponseDto toAuthorizationAdminDto(Authorization authorization) {
        return new AuthorizationAdminResponseDto(
                authorization.getId(),
                toWalletAdminDto(authorization.getWallet()),
                authorization.getStripeAuthorizationId(),
                toCardAdminDto(authorization.getCard()),
                authorization.getApproved(),
                authorization.getAmount(),
                authorization.getCurrency(),
                authorization.getStatus().toString(),
                authorization.getMerchantName(),
                authorization.getMerchantCategory(),
                authorization.getMerchantCity(),
                authorization.getMerchantCountry(),
                authorization.getCreatedAt());
    }

    public DisputeAdminResponseDto toDisputeAdminDto(Dispute dispute) {
        return new DisputeAdminResponseDto(
                dispute.getId(),
                toTransactionAdminDto(dispute.getTransaction()),
                toWalletAdminDto(dispute.getWallet()),
                dispute.getStripeDisputeId(),
                dispute.getAmount(),
                dispute.getCurrency(),
                dispute.getStatus().toString(),
                dispute.getReason().toString(),
                dispute.getCreatedAt(),
                dispute.getResolvedAt());
    }

    public ExternalAccountAdminResponseDto toExternalAccountAdminDto(ExternalAccount externalAccount) {
        return new ExternalAccountAdminResponseDto(
                externalAccount.getId(),
                toAccountAdminDto(externalAccount.getAccount()),
                externalAccount.getStripeExternalAccountId(),
                externalAccount.getBankName(),
                externalAccount.getLast4(),
                externalAccount.getCountry(),
                externalAccount.getCurrency(),
                externalAccount.isDefault(),
                externalAccount.isDeleted(),
                externalAccount.getDeletedAt(),
                externalAccount.getCreatedAt());
    }

    public PlanResponseDto toPlanDto(Plan plan) {
        PlanStatus status = plan.getStatus() == null ? PlanStatus.PUBLISHED : plan.getStatus();

        return new PlanResponseDto(
                plan.getId(),
                plan.getName(),
                plan.getCode(),
                plan.getDescription(),
                plan.isActive(),
                status,
                status == PlanStatus.DRAFT && plan.isPublishable(),
                plan.getCreatedAt(),
                plan.getDefaultPrice()
        );
    }

    public PaymentMethodResponseDto toPaymentMethodDto(com.stripe.model.PaymentMethod paymentMethod) {
        return new PaymentMethodResponseDto(
                paymentMethod.getId(),
                paymentMethod.getCard().getBrand(),
                paymentMethod.getCard().getLast4(),
                String.valueOf(paymentMethod.getCard().getExpMonth()),
                String.valueOf(paymentMethod.getCard().getExpYear())
        );
    }

    public SubscriptionResponseDto toSubscriptionDto(Subscription subscription) {
        com.stripe.model.PaymentMethod paymentMethod = getPaymentMethodUseCase.execute(
                subscription.getUser().getStripeCustomerId(),
                subscription.getStripePaymentMethodId()
        );

        return toSubscriptionDto(subscription, toPaymentMethodDto(paymentMethod));
    }

    public SubscriptionResponseDto toSubscriptionDtoWithOptionalPaymentMethod(Subscription subscription) {
        String paymentMethodId = subscription.getStripePaymentMethodId();
        String customerId = subscription.getUser() == null
                ? null
                : subscription.getUser().getStripeCustomerId();

        if (paymentMethodId == null || paymentMethodId.isBlank()
                || customerId == null || customerId.isBlank()) {
            return toSubscriptionDto(subscription, null);
        }

        try {
            com.stripe.model.PaymentMethod paymentMethod = getPaymentMethodUseCase.execute(
                    customerId,
                    paymentMethodId
            );
            return toSubscriptionDto(subscription, toPaymentMethodDto(paymentMethod));
        } catch (RuntimeException exception) {
            log.warn(
                    "Could not enrich subscription {} with Stripe payment method {}; returning it without payment details",
                    subscription.getId(),
                    paymentMethodId,
                    exception
            );
            return toSubscriptionDto(subscription, null);
        }
    }

    private SubscriptionResponseDto toSubscriptionDto(
            Subscription subscription,
            PaymentMethodResponseDto paymentMethod
    ) {
        return new SubscriptionResponseDto(
                subscription.getId(),
                subscription.getUser(),
                subscription.getAccount(),
                subscription.getPlan(),
                subscription.getPrice(),
                paymentMethod,
                subscription.getStatus(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.isCancelAtPeriodEnd(),
                subscription.getCanceledAt(),
                subscription.getScheduledPlan(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
