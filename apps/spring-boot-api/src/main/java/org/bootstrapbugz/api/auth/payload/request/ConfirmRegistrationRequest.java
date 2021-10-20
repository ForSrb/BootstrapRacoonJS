package org.bootstrapbugz.api.auth.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmRegistrationRequest {
  @NotBlank(message = "{token.invalid}")
  private String accessToken;
}