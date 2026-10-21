package com.jexis.jexis_backend.account.application.useCases;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;

@Service
public class GetUserAccountsUseCase {
    AccountRepository repo;

    public GetUserAccountsUseCase(AccountRepository repo) {
        this.repo = repo;
    }

    /**
     * Fetch paginated user accounts, optionally filtered by search term.
     *
     * @param id owner id
     * @param page 1-based page number
     * @param pageSize size of page
     * @param search optional search string
     * @return paginated accounts for the owner
     */
    public Page<Account> execute(UUID id, int page, int pageSize, String search) {
        int p = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(p, pageSize);

        if (search == null || search.isBlank()) {
            return repo.findAllByOwnerIdAndIsDeletedFalse(id, pageable);
        }

        return repo.searchByOwner(id, search.trim(), pageable);
    }
}
