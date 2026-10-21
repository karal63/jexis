package com.jexis.jexis_backend.account.application.useCases;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;

@Service
public class GetMemberAccountsUseCase {
    private final AccountRepository repo;

    public GetMemberAccountsUseCase(AccountRepository repo) {
        this.repo = repo;
    }

    public Page<Account> execute(UUID userId, int page, int pageSize, String search) {
        int p = Math.max(0, page - 1);

        Pageable pageable = PageRequest.of(p, pageSize);
        return repo.searchAccountsByMember(userId, normalize(search), pageable);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
