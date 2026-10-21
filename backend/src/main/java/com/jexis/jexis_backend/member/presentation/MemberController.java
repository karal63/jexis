package com.jexis.jexis_backend.member.presentation;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.member.application.dto.MemberPageResponseDto;
import com.jexis.jexis_backend.member.application.dto.MemberPageAdminResponseDto;
import com.jexis.jexis_backend.member.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.member.application.dto.CreateMemberDto;
import com.jexis.jexis_backend.member.application.dto.EditMemberDto;
import com.jexis.jexis_backend.member.application.dto.MemberResponseDto;
import com.jexis.jexis_backend.member.application.useCases.AddMemberUseCase;
import com.jexis.jexis_backend.member.application.useCases.EditMemberUseCase;
import com.jexis.jexis_backend.member.application.useCases.GetAccountMembersUseCase;
import com.jexis.jexis_backend.member.application.useCases.GetMemberUseCase;
import com.jexis.jexis_backend.member.application.useCases.GetMembersUseCase;
import com.jexis.jexis_backend.member.application.useCases.RemoveMemberUseCase;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.account.application.useCases.GetMemberAccountsUseCase;
import com.jexis.jexis_backend.account.application.dto.PaginatedAccountsResponseDto;
import com.jexis.jexis_backend.account.application.dto.AccountResponseDto;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class MemberController {
    private final GetMemberUseCase getMemberUseCase;
    private final GetMembersUseCase getMembersUseCase;
    private final AddMemberUseCase addMemberUseCase;
    private final EditMemberUseCase editMemberUseCase;
    private final RemoveMemberUseCase removeMemberUseCase;
    private final DtoHelper dtoHelper;
    private final GetAccountMembersUseCase getAccountMembersUseCase;
    private final GetMemberAccountsUseCase getMemberAccountsUseCase;

    @GetMapping("/admin/members")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public MemberPageAdminResponseDto listAccountMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Member> membersPage = getMembersUseCase.execute(page,
                pageSize, search, role, sortBy, sortDirection);

        return mapToPageAdminResponse(membersPage, page, pageSize);
    }

    @PostMapping("/members/add")
    @PreAuthorize("@memberAuthorization.canCreate(authentication.principal.id(), #body.accountId)")
    public MemberResponseDto add(@Valid @RequestBody CreateMemberDto body) {
        Member member = addMemberUseCase.execute(body);
        return dtoHelper.toMemberDto(member);
    }

    private MemberPageResponseDto mapToPageResponse(Page<Member> membersPage, int page, int pageSize) {
        List<MemberResponseDto> items = membersPage.getContent().stream()
                .map(dtoHelper::toMemberDto)
                .toList();
        return new MemberPageResponseDto(items, page, pageSize,
                membersPage.getTotalElements(), membersPage.getTotalPages());
    }

    private MemberPageAdminResponseDto mapToPageAdminResponse(Page<Member> membersPage, int page, int pageSize) {
        var items = membersPage.getContent().stream()
                .map(dtoHelper::toMemberAdminDto)
                .toList();
        return new MemberPageAdminResponseDto(items, page, pageSize,
                membersPage.getTotalElements(), membersPage.getTotalPages());
    }

    @GetMapping("/accounts/{id}/members")
    @PreAuthorize("@memberAuthorization.canView(authentication.principal.id(), #id)")
    public MemberPageResponseDto getMembersByAccount(@PathVariable UUID id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<Member> membersPage = getAccountMembersUseCase.execute(id,
                page, pageSize, search, role, sortBy, sortDirection);

        List<MemberResponseDto> items = membersPage.getContent().stream()
                .map(dtoHelper::toMemberDto)
                .toList();

        return new MemberPageResponseDto(items, page, pageSize,
                membersPage.getTotalElements(), membersPage.getTotalPages());
    }

    @GetMapping("/accounts/me")
    public PaginatedAccountsResponseDto getMyAccounts(@AuthenticationPrincipal AuthUser authUser,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize,
                                                      @RequestParam(required = false) String search) {
        Page<Account> accountsPage = getMemberAccountsUseCase.execute(authUser.id(), page, pageSize, search);

        List<AccountResponseDto> items = accountsPage
                .stream()
                .map(dtoHelper::toAccountDto)
                .toList();

        return new PaginatedAccountsResponseDto(
                items,
                page,
                pageSize,
                accountsPage.getTotalElements(),
                accountsPage.getTotalPages());
    }

    @GetMapping("/accounts/{id}/members/{memberId}")
    @PreAuthorize("@memberAuthorization.canView(authentication.principal.id(), #id, #memberId)")
    public MemberResponseDto get(@PathVariable UUID id, @PathVariable UUID memberId) {
        Member member = getMemberUseCase.execute(memberId);
        return dtoHelper.toMemberDto(member);
    }

    @PatchMapping("/accounts/{id}/members/{memberId}/edit")
    @PreAuthorize("@memberAuthorization.canEdit(authentication.principal.id(), #id, #memberId)")
    public MemberResponseDto edit(@PathVariable UUID id, @PathVariable UUID memberId, @Valid @RequestBody EditMemberDto body) {
        Member member = editMemberUseCase.execute(memberId, body);
        return dtoHelper.toMemberDto(member);
    }

    @PostMapping("/accounts/{id}/members/{memberId}/remove")
    @PreAuthorize("@memberAuthorization.canDelete(authentication.principal.id(), #id, #memberId)")
    public void remove(@PathVariable UUID id, @PathVariable UUID memberId) {
        removeMemberUseCase.execute(memberId);
    }
}
