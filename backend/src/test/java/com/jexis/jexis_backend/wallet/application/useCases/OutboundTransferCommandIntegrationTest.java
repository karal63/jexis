package com.jexis.jexis_backend.wallet.application.useCases;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.enums.UserRole;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.infrastructure.*;
import com.jexis.jexis_backend.wallet.application.dto.CreateOutboundTransferDto;

@ActiveProfiles("test")
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:transfer_commands;MODE=PostgreSQL;NON_KEYWORDS=KEY,VALUE,INTERVAL;DB_CLOSE_DELAY=-1",
    "management.health.rabbit.enabled=false"})
class OutboundTransferCommandIntegrationTest {
    @Autowired OutboundTransferCommands commands;
    @Autowired OutboundTransferCommandRepository repo;
    @Autowired UserRepository users;
    @Autowired AccountRepository accounts;
    @Autowired WalletRepository wallets;
    @Autowired PlatformTransactionManager transactions;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.jexis.jexis_backend.emailService.infrastructure.messaging.EmailProducer emailProducer;

    @Test void concurrentPreparationCommitsOneIdentityAndSurvivesCallerRollback() throws Exception {
        String unique = UUID.randomUUID().toString();
        User user = users.saveAndFlush(new User("Transfer", "Test", unique + "@example.com", unique,
                "hash", null, List.of(UserRole.USER)));
        Account account = accounts.saveAndFlush(new Account(user.getEmail(), "acct_" + unique, "link", user));
        Wallet wallet = wallets.saveAndFlush(new Wallet("Test", "fa_" + unique, account));
        var destination = new ExternalAccount(account, "pm_1", "Bank", "1234", "US", "usd", true);
        UUID key = UUID.randomUUID();
        var body = new CreateOutboundTransferDto(100L, "usd", "Test", UUID.randomUUID());
        var executor = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            Callable<UUID> prepare = () -> {
                start.await();
                return commands.prepare(wallet, destination, body, key).getId();
            };
            var first = executor.submit(prepare);
            var second = executor.submit(prepare);
            start.countDown();
            UUID id = first.get(10, TimeUnit.SECONDS);
            assertEquals(id, second.get(10, TimeUnit.SECONDS));
            assertEquals(id, repo.findByWalletIdAndRequestKey(wallet.getId(), key).orElseThrow().getId());
        } finally {
            executor.shutdownNow();
        }
        UUID anotherKey = UUID.randomUUID();
        assertThrows(IllegalStateException.class, () -> new TransactionTemplate(transactions).execute(status -> {
            commands.prepare(wallet, destination, body, anotherKey);
            throw new IllegalStateException("Remote response lost");
        }));
        var stored = repo.findByWalletIdAndRequestKey(wallet.getId(), anotherKey).orElseThrow();
        commands.complete(stored.getId(), "obt_1");
        assertEquals("obt_1", repo.findById(stored.getId()).orElseThrow().getStripeTransferId());
    }
}
