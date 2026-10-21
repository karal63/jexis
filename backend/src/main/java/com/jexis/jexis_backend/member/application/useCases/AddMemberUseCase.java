package com.jexis.jexis_backend.member.application.useCases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.application.useCases.CheckAccountResourceLimitUseCase;
import com.jexis.jexis_backend.account.domain.enums.AccountResource;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.member.application.dto.CreateMemberDto;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.exceptions.MemberExistsException;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;

@Service
public class AddMemberUseCase {
    private final MemberRepository repo;
    private final GetAccountUseCase getAccountUseCase;
    private final GetUserUseCase getUserUseCase;
    private final LockAccountUseCase lockAccount;
    private final CheckAccountResourceLimitUseCase checkLimit;

    public AddMemberUseCase(MemberRepository repo, GetAccountUseCase getAccountUseCase, GetUserUseCase getUserUseCase,
            LockAccountUseCase lockAccount, CheckAccountResourceLimitUseCase checkLimit) {
        this.lockAccount = lockAccount;
        this.checkLimit = checkLimit;
        this.repo = repo;
        this.getAccountUseCase = getAccountUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    @Transactional
    public Member execute(CreateMemberDto body) {
        lockAccount.execute(body.accountId());
        repo.findByAccountIdAndUserId(body.accountId(), body.userId()).ifPresent(member -> {
            throw new MemberExistsException();
        });

        Account account = getAccountUseCase.execute(body.accountId());
        User user = getUserUseCase.execute(body.userId());

        checkLimit.requireCapacity(body.accountId(), AccountResource.MEMBERS, 1);

        Member member = new Member(account, user, body.role());
        Member savedMember = repo.save(member);
        return savedMember;

    }
}
