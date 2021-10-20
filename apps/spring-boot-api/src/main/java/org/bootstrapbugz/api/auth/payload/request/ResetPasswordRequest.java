package org.bootstrapbugz.api.auth.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bootstrapbugz.api.auth.validator.FieldMatch;
import org.bootstrapbugz.api.shared.constants.Regex;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldMatch(first = "password", second = "confirmPassword", message = "{password.doNotMatch}")
public class ResetPasswordRequest {
  @NotBlank(message = "{token.invalid}")
  private String accessToken;

  @Pattern(regexp = Regex.PASSWORD, message = "{password.invalid}")
  private String password;

  @Pattern(regexp = Regex.PASSWORD, message = "{password.invalid}")
  private String confirmPassword;
}