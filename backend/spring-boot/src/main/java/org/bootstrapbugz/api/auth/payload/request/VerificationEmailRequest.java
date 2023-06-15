package org.bootstrapbugz.api.auth.payload.request;

import jakarta.validation.constraints.NotBlank;
import org.bootstrapbugz.api.shared.validator.UsernameOrEmail;

public record VerificationEmailRequest(
    @NotBlank @UsernameOrEmail(message = "{usernameOrEmail.invalid}") String usernameOrEmail) {}
