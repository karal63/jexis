package com.jexis.jexis_backend.transaction.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.GetStripeTransactionUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.RetrieveFinancialAccountUseCase;
import com.jexis.jexis_backend.transaction.application.dto.CreateBankTransactionDto;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.PaymentMethod;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletByFAIdUseCase;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.stripe.model.Event;
import com.stripe.model.treasury.FinancialAccount;
import com.stripe.model.treasury.ReceivedCredit;
import org.springframework.stereotype.Service;

@Service
public class CreateBankTransactionUseCase {
    private final GetStripeTransactionUseCase getStripeTransactionUseCase;
    private final GetWalletByFAIdUseCase getWalletByFAIdUseCase;
    private final TransactionRepository repo;
    private final RetrieveFinancialAccountUseCase retrieveFinancialAccountUseCase;
    private final SyncBalanceUseCase syncBalanceUseCase;

    public CreateBankTransactionUseCase(GetStripeTransactionUseCase getStripeTransactionUseCase, GetWalletByFAIdUseCase getWalletByFAIdUseCase,
                                        TransactionRepository repo, RetrieveFinancialAccountUseCase retrieveFinancialAccountUseCase, SyncBalanceUseCase syncBalanceUseCase) {
        this.getStripeTransactionUseCase = getStripeTransactionUseCase;
        this.getWalletByFAIdUseCase = getWalletByFAIdUseCase;
        this.repo = repo;
        this.retrieveFinancialAccountUseCase = retrieveFinancialAccountUseCase;
        this.syncBalanceUseCase = syncBalanceUseCase;
    }

    public void execute(String accountId, ReceivedCredit credit) {
        com.stripe.model.treasury.Transaction treasuryTransaction = getStripeTransactionUseCase.execute(accountId, credit.getTransaction());
        syncBalanceUseCase.execute(accountId, treasuryTransaction.getFinancialAccount());

        Wallet wallet = getWalletByFAIdUseCase.execute(credit.getFinancialAccount());

        Transaction transaction = new Transaction(
                wallet,
                treasuryTransaction.getId(),
                credit.getId(),
                TransactionType.INBOUND_TRANSFER,
                credit.getAmount(),
                credit.getCurrency(),
                TransactionStatus.COMPLETED,
                TransactionDirection.CREDIT
        );

        transaction.setBankName(credit.getInitiatingPaymentMethodDetails().getUsBankAccount().getBankName());
        transaction.setBankAccountLast4(credit.getInitiatingPaymentMethodDetails().getUsBankAccount().getLast4());
        transaction.setRoutingNumber(credit.getInitiatingPaymentMethodDetails().getUsBankAccount().getRoutingNumber());
        transaction.setPaymentMethod(PaymentMethod.valueOf(credit.getNetwork().toUpperCase()));

        repo.save(transaction);
    }
}
