package com.jexis.jexis_backend.invoice.application.security;

import com.jexis.jexis_backend.invoice.application.useCases.GetInvoiceUseCase;
import com.jexis.jexis_backend.invoice.domain.entities.Invoice;
import com.jexis.jexis_backend.member.application.useCases.HasRoleUseCase;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.subscription.application.useCases.GetSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.user.application.security.UserAuthorization;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InvoiceAuthorization {
    private final GetInvoiceUseCase getInvoiceUseCase;
    private final HasRoleUseCase hasRoleUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final UserAuthorization userAuthorization;

    public InvoiceAuthorization(GetInvoiceUseCase getInvoiceUseCase, HasRoleUseCase hasRoleUseCase, GetSubscriptionUseCase getSubscriptionUseCase, UserAuthorization userAuthorization) {
        this.getInvoiceUseCase = getInvoiceUseCase;
        this.hasRoleUseCase = hasRoleUseCase;
        this.getSubscriptionUseCase = getSubscriptionUseCase;
        this.userAuthorization = userAuthorization;
    }

    public boolean canView(UUID userId, UUID subscriptionId) {
        Subscription subscription = getSubscriptionUseCase.execute(subscriptionId);
        UUID accountId = subscription.getAccount().getId();
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN)
                || userAuthorization.isAdmin(userId);
    }

    public boolean canPay(UUID userId, UUID invoiceId) {
        Invoice invoice = getInvoiceUseCase.execute(invoiceId);
        UUID accountId = invoice.getSubscription().getAccount().getId();
        return hasRoleUseCase.execute(userId, accountId, Role.OWNER)
                || userAuthorization.isAdmin(userId)
                || hasRoleUseCase.execute(userId, accountId, Role.ADMIN);
    }

}
