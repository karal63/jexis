package com.jexis.jexis_backend.user.presentation;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.user.application.dto.*;
import com.jexis.jexis_backend.user.application.useCases.*;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import jakarta.validation.Valid;

/**
 * UserController
 * <p>
 * REST controller in the presentation layer responsible for exposing
 * user-related HTTP endpoints.
 * <p>
 * It handles request routing, input validation, and response mapping,
 * delegating all business logic execution to dedicated user use case
 * services (application layer).
 * <p>
 * This class does not contain domain logic; its role is limited to
 * orchestrating request/response flow between the client and the
 * application layer.
 * <p>
 * Author: Leo
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class UserController {

    private final GetUsersUseCase getUsersUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final EditUserUseCase editUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final GetUserUseCase getUserUseCase;
    private final DtoHelper dtoHelper;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final RequestPasswordChangeUseCase requestPasswordChangeUseCase;
    private final SendActivationLinkUseCase sendActivationLinkUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final GetPaymentMethodsUseCase getPaymentMethodsUseCase;
    private final CreatePaymentMethodSetupUseCase createPaymentMethodSetupUseCase;
    private final DeletePaymentMethodUseCase deletePaymentMethodUseCase;
    private @Value("${application.origin}") String applicationOrigin;

    /**
     * Returns a list of all users.
     * <p>
     * This endpoint retrieves all existing users by delegating to the
     * getUsersUseCase, which interacts with the repository to fetch the data.
     * <p>
     * Endpoint: GET /user/list
     *
     * @return list of all accounts
     */
    @GetMapping("/admin/users")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public UserPageAdminResponseDto getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) com.jexis.jexis_backend.user.domain.enums.UserRole role,
            @RequestParam(required = false) Boolean isActivated,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        Page<User> pageResult =
                getUsersUseCase.execute(page, pageSize, search, role, isActivated, sortBy, sortDirection);
        return mapToPageAdminResponse(pageResult, page, pageSize);
    }

    /**
     * Retrieves a single user by their identifier.
     * <p>
     * Endpoint: GET /user/list/{id}
     *
     * @param id the unique identifier of the user to retrieve
     * @return the matching user entity
     */
    @GetMapping("/admin/users/{id}")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public UserAdminResponseDto getUser(@PathVariable UUID id) {
        return dtoHelper.toUserAdminDto(getUserUseCase.execute(id));
    }

    /**
     * Creates a new user account.
     * <p>
     * Endpoint: POST /user/create
     *
     * @param body the request payload containing user creation details
     * @return the newly created user entity
     */
    @PostMapping("/admin/users/create")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public UserAdminResponseDto createUsers(@Valid @RequestBody AdminCreateDto body) {
        CreateDto dto = new CreateDto(body.getFirstName(), body.getLastName(), body.getEmail(), body.getPhoneNumber(),
                body.getPassword());
        return dtoHelper.toUserAdminDto(createUserUseCase.execute(dto, body.getRoles()));
    }

    /**
     * Deletes a user by their identifier.
     * <p>
     * Endpoint: DELETE /user/delete/{id}
     *
     * @param id the unique identifier of the user to delete
     * @return a confirmation message after successful deletion
     */
    @PostMapping("/users/{id}/delete")
    @PreAuthorize("@userAuthorization.canDelete(authentication.principal.id(), #id)")
    public String deleteUser(@PathVariable UUID id) {
        deleteUserUseCase.execute(id);
        return "User deleted successfully";
    }

    /**
     * Updates an existing user with the provided changes.
     * Endpoint: PATCH /user/edit/{id}
     *
     * @param editDto the user update payload
     * @param id      the unique identifier of the user to update
     * @return an optional updated user entity
     */
    @PatchMapping("/users/{id}/edit")
    @PreAuthorize("@userAuthorization.canEdit(authentication.principal.id(), #id)")
    public UserResponseDto editUser(@Valid @RequestBody EditDto editDto, @PathVariable UUID id) {
        return dtoHelper.toUserDto(editUserUseCase.execute(id, editDto));
    }

    /**
     * Changes a user's password.
     * <p>
     * Endpoint: PATCH /admin/users/{id}/password
     *
     * @param passwordDto the request payload containing the new password
     * @param id          the unique identifier of the user whose password will be updated
     * @return the updated user entity
     */
    @PatchMapping("/admin/users/{id}/password")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public UserAdminResponseDto changePassword(@Valid @RequestBody ChangePasswordDto passwordDto, @PathVariable UUID id) {
        return dtoHelper.toUserAdminDto(changePasswordUseCase.execute(id, passwordDto.getPassword()));
    }

    @PostMapping("/users/change-password")
    public ResponseEntity<String> requestPasswordReset(@Valid @RequestBody RequestPasswordChangeDto body) {
        requestPasswordChangeUseCase.execute(body);
        return ResponseEntity.ok("We sent your password reset link to your email");
    }

    @PostMapping("/users/reset-password/confirm")
    public ResponseEntity<String> confirmPasswordReset(@Valid @RequestBody ConfirmPasswordResetDto body) {
        confirmPasswordResetUseCase.execute(body);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @GetMapping("/user/activate")
    public ResponseEntity<Void> sendActivationLink(@RequestParam String token) {
        activateUserUseCase.execute(token);

        String redirectUrl = "%s/dashboard".formatted(applicationOrigin);
        return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
    }

    @GetMapping("/me/payment_methods")
    public List<PaymentMethodResponseDto> getPaymentMethods(@AuthenticationPrincipal AuthUser user) {
        List<PaymentMethod> paymentMethods = getPaymentMethodsUseCase.execute(user.id());
        return paymentMethods.stream().map(dtoHelper::toPaymentMethodDto).toList();
    }

    /**
     * Set up a new payment method for the customer.
     * <p>
     * Endpoint: POST /me/payment-methods/setup
     *
     * @TODO
     * @apiNote This endpoint implementation must be updated in the future.
     * @apiNote Currently, it returns a Stripe customer portal session URL,
     * but it should be modified to return a Stripe key for stripe.js and use SetupIntentCreateParams.
     */
    @PostMapping("/me/payment-methods/setup")
    public String createPaymentMethodSetup(@AuthenticationPrincipal AuthUser user) {
        return createPaymentMethodSetupUseCase.execute(user.id());
    }

    @DeleteMapping("/me/payment-methods/{paymentMethodId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detachPaymentMethod(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String paymentMethodId
    ) {
        deletePaymentMethodUseCase.execute(user.id(), paymentMethodId);
    }

    private UserPageAdminResponseDto mapToPageAdminResponse(org.springframework.data.domain.Page<com.jexis.jexis_backend.user.domain.entities.User> usersPage, int page, int pageSize) {
        java.util.List<UserAdminResponseDto> items = usersPage.getContent().stream()
                .map(dtoHelper::toUserAdminDto)
                .toList();
        return new UserPageAdminResponseDto(
                items,
                page,
                pageSize,
                usersPage.getTotalElements(),
                usersPage.getTotalPages());
    }

    private UserPageResponseDto mapToPageResponse(org.springframework.data.domain.Page<com.jexis.jexis_backend.user.domain.entities.User> usersPage, int page, int pageSize) {
        java.util.List<UserResponseDto> items = usersPage.getContent().stream()
                .map(dtoHelper::toUserDto)
                .toList();
        return new UserPageResponseDto(
                items,
                page,
                pageSize,
                usersPage.getTotalElements(),
                usersPage.getTotalPages());
    }
}
