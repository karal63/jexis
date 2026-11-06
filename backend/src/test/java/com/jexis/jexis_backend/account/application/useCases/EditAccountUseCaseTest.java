package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import com.jexis.jexis_backend.stripe.application.useCases.RetrieveAccountByIdUseCase;
import com.stripe.model.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditAccountUseCaseTest {
    @Mock AccountRepository accountRepo;
    @Mock RetrieveAccountByIdUseCase retrieveAccountByIdUseCase;
    @Mock GetAccountByStripeIdUseCase getAccountByStripeIdUseCase;
    @InjectMocks EditAccountUseCase useCase;

    @ParameterizedTest
    @ValueSource(strings = {"company", "non_profit", "government_entity"})
    void copiesOrganizationDetails(String businessType) {
        var stripe = mock(com.stripe.model.Account.class, RETURNS_DEEP_STUBS);
        var address = mock(Address.class);
        var account = new Account();
        when(retrieveAccountByIdUseCase.execute("acct_123")).thenReturn(stripe);
        when(stripe.getId()).thenReturn("acct_123");
        when(getAccountByStripeIdUseCase.execute("acct_123")).thenReturn(account);
        when(stripe.getBusinessType()).thenReturn(businessType);
        when(stripe.getCompany().getName()).thenReturn("Organization");
        when(stripe.getBusinessProfile().getName()).thenReturn("Trading name");
        when(stripe.getCompany().getAddress()).thenReturn(address);
        when(address.getCity()).thenReturn("Warsaw");
        when(stripe.getCompany().getPhone()).thenReturn("+48123456789");

        useCase.execute("acct_123");

        assertThat(account.getFirstName()).isEqualTo("Organization");
        assertThat(account.getLastName()).isEqualTo("Trading name");
        assertThat(account.getCity()).isEqualTo("Warsaw");
        assertThat(account.getPhone()).isEqualTo("+48123456789");
        verify(accountRepo).save(account);
    }

    @Test
    void copiesIndividualDetailsFromIndividual() {
        var stripe = mock(com.stripe.model.Account.class, RETURNS_DEEP_STUBS);
        var address = mock(Address.class);
        var account = new Account();
        when(retrieveAccountByIdUseCase.execute("acct_123")).thenReturn(stripe);
        when(stripe.getId()).thenReturn("acct_123");
        when(getAccountByStripeIdUseCase.execute("acct_123")).thenReturn(account);
        when(stripe.getBusinessType()).thenReturn("individual");
        when(stripe.getIndividual().getFirstName()).thenReturn("Alex");
        when(stripe.getIndividual().getLastName()).thenReturn("Smith");
        when(stripe.getIndividual().getAddress()).thenReturn(address);
        when(address.getCity()).thenReturn("Krakow");
        when(stripe.getIndividual().getPhone()).thenReturn("+48987654321");

        useCase.execute("acct_123");

        assertThat(account.getFirstName()).isEqualTo("Alex");
        assertThat(account.getLastName()).isEqualTo("Smith");
        assertThat(account.getCity()).isEqualTo("Krakow");
        assertThat(account.getPhone()).isEqualTo("+48987654321");
        verify(accountRepo).save(account);
    }
}
