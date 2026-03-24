package com.pfitztronic.iothub.core.accounts.controllers;

import com.pfitztronic.iothub.core.accounts.dto.*;
import com.pfitztronic.iothub.core.accounts.mappers.DtoMapper;
import com.pfitztronic.iothub.core.accounts.models.User;
import com.pfitztronic.iothub.core.accounts.services.impl.UserManagementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    /**
     * Constructs a UserManagementController with the required service.
     *
     * @param userManagementService the service for handling user management operations
     */
    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    /**
     * Creates a new user account.
     *
     * @param data the user creation request data containing phone number, name, and password
     * @return a NewUserResponse containing the created user's details
     */
    @PostMapping
    public NewUserResponse createUser(@Valid @RequestBody CreateUserAccountData data) {
        User user = userManagementService.createNewUser(
                data.phoneNumber(), data.userName(), data.password()
        );
        return DtoMapper.toNewUserResponse(user);
    }

    /**
     * Updates the authenticated user's name details.
     * Only the user themselves can update their own details.
     *
     * @param userId the phone number of the user (must match authenticated user)
     * @param data the user details change request containing the new name
     * @return a NewUserResponse containing the updated user's details
     */
    @PreAuthorize("@authService.verifyAuthedUserId(#userId)")
    @PatchMapping("{userId}/details")
    public NewUserResponse changeDetails(
            @PathVariable String userId,
            @Valid @RequestBody ChangeUserDetailsData data
    ) {
        User updated = userManagementService.changeUserDetails(userId, data.userName());
        return DtoMapper.toNewUserResponse(updated);
    }

    /**
     * Changes the authenticated user's password after verifying the old password.
     * Only the user themselves can change their own password.
     *
     * @param userId the phone number of the user (must match authenticated user)
     * @param data the password change request containing old and new passwords
     * @return a success message
     */
    @PreAuthorize("@authService.verifyAuthedUserId(#userId)")
    @PatchMapping("{userId}/password")
    public String changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordData data
    ) {
        userManagementService.changePassword(userId, data.oldPassword(), data.newPassword());
        return "Password updated";
    }

    /**
     * Requests a password reset code to be sent to the user.
     * Only the user themselves can request their own password reset.
     *
     * @param userId the phone number of the user (must match authenticated user)
     * @return a success message
     */
    @PreAuthorize("@authService.verifyAuthedUserId(#userId)")
    @PostMapping("{userId}/password-reset")
    public String requestPasswordReset(@PathVariable String userId) {
        userManagementService.requestPasswordResetCode(userId);
        return "Password reset code sent";
    }

    @PreAuthorize("@authService.verifyAuthedUserId(#userId)")
    @PostMapping("{userId}/password-reset/confirm")
    public String changePasswordWithResetCode(
            @PathVariable String userId,
            @Valid @RequestBody ResetPasswordWithCodeData data
    ) {
        userManagementService.changePasswordWithResetCode(userId, data.code(), data.newPassword());
        return "Password updated";
    }

    @PreAuthorize("@authService.verifyAuthedUserId(#userId)")
    @PostMapping("{userId}/verify")
    public String verifyUser(
            @PathVariable String userId,
            @Valid @RequestBody VerifyUserAccountData data
    ) {
        userManagementService.verifyUserAccount(userId, data.code());
        return "User verified";
    }

    @PreAuthorize("@authService.verifyAuthedUserId(#userId)")
    @PostMapping("{userId}/verify/resend")
    public String resendVerification(@PathVariable String userId) {
        userManagementService.resendVerificationCode(userId);
        return "Verification code resent";
    }
}
