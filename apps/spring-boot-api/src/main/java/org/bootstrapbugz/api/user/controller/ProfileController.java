package org.bootstrapbugz.api.user.controller;

import org.bootstrapbugz.api.shared.constants.Path;
import org.bootstrapbugz.api.user.payload.request.ChangePasswordRequest;
import org.bootstrapbugz.api.user.payload.request.UpdateProfileRequest;
import org.bootstrapbugz.api.user.payload.response.UserResponse;
import org.bootstrapbugz.api.user.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(Path.PROFILE)
public class ProfileController {
  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @PutMapping("/update")
  public ResponseEntity<UserResponse> update(
      @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
    return ResponseEntity.ok(profileService.update(updateProfileRequest));
  }

  @PutMapping("/change-password")
  public ResponseEntity<Void> changePassword(
      @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
    profileService.changePassword(changePasswordRequest);
    return ResponseEntity.noContent().build();
  }
}