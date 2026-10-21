package com.jexis.jexis_backend.account.application.useCases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;

/**
 * GetAccountsUseCase
 *
 * This service class implements the use case for retrieving accounts with
 * pagination and optional search. Returns a Page<Account> so callers can
 * access paging metadata.
 *
 * Author: Leo
 */
@Service
public class GetAccountsUseCase {
    AccountRepository repo;

    public GetAccountsUseCase(AccountRepository repo) {
        this.repo = repo;
    }

    /**
     * Fetch paginated accounts, optionally filtered by search term.
     *
     * @param page 1-based page number
     * @param pageSize size of page
     * @param search optional search string
     * @return paginated accounts
     */
    public Page<Account> execute(int page, int pageSize, String search) {
        int p = Math.max(0, page - 1);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        Pageable pageable = PageRequest.of(p, pageSize, sort);

        if (search == null || search.isBlank()) {
            return repo.findAllByIsDeletedFalse(pageable);
        }

        return repo.searchAll(search.trim(), pageable);
    }
}
