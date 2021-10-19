package org.bootstrapbugz.api.shared.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bootstrapbugz.api.admin.payload.request.AdminRequest;
import org.bootstrapbugz.api.admin.payload.request.ChangeRoleRequest;
import org.bootstrapbugz.api.auth.payload.request.SignInRequest;
import org.bootstrapbugz.api.auth.util.AuthUtil;
import org.bootstrapbugz.api.shared.config.DatabaseContainers;
import org.bootstrapbugz.api.shared.constants.Path;
import org.bootstrapbugz.api.shared.error.ErrorDomain;
import org.bootstrapbugz.api.shared.error.response.ErrorResponse;
import org.bootstrapbugz.api.shared.util.TestUtil;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.bootstrapbugz.api.user.payload.request.ChangePasswordRequest;
import org.bootstrapbugz.api.user.payload.request.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class AccessingResourcesTest extends DatabaseContainers {
  private final ErrorResponse expectedForbiddenResponse =
      new ErrorResponse(HttpStatus.FORBIDDEN, ErrorDomain.AUTH, "Access is denied");
  private final ErrorResponse expectedUnauthorizedResponse =
      new ErrorResponse(HttpStatus.UNAUTHORIZED, ErrorDomain.AUTH, "Unauthorized");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void findUserByUsernameShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var resultActions =
        mockMvc
            .perform(
                get(Path.USERS + "/{username}", "unknown").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @Test
  void updateUserShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var updateUserRequest =
        new UpdateUserRequest("Updated", "Updated", "forUpdate2", "forUpdate2@localhost.com");
    var resultActions =
        mockMvc
            .perform(
                put(Path.USERS + "/update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateUserRequest)))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @Test
  void changePasswordShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var changePasswordRequest = new ChangePasswordRequest("qwerty123", "qwerty1234", "qwerty1234");
    var resultActions =
        mockMvc
            .perform(
                put(Path.USERS + "/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(changePasswordRequest)))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @Test
  void findAllUsersShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var resultActions =
        mockMvc
            .perform(get(Path.ADMIN + "/users").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @Test
  void findAllUsersShouldThrowForbidden_signedInUserIsNotAdmin() throws Exception {
    var signInResponse =
        TestUtil.signIn(mockMvc, objectMapper, new SignInRequest("user", "qwerty123"));
    var resultActions =
        mockMvc
            .perform(
                get(Path.ADMIN + "/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(AuthUtil.AUTH_HEADER, signInResponse.getAccessToken()))
            .andExpect(status().isForbidden());
    TestUtil.checkErrorMessages(expectedForbiddenResponse, resultActions);
  }

  @Test
  void changeUsersRolesShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var changeRoleRequest =
        new ChangeRoleRequest(Set.of("user"), Set.of(RoleName.USER, RoleName.ADMIN));
    var resultActions =
        mockMvc
            .perform(
                put(Path.ADMIN + "/users/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(changeRoleRequest)))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @Test
  void changeUsersRolesShouldThrowForbidden_signedInUserIsNotAdmin() throws Exception {
    var signInResponse =
        TestUtil.signIn(mockMvc, objectMapper, new SignInRequest("user", "qwerty123"));
    var changeRoleRequest =
        new ChangeRoleRequest(Set.of("user"), Set.of(RoleName.USER, RoleName.ADMIN));
    var resultActions =
        mockMvc
            .perform(
                put(Path.ADMIN + "/users/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(AuthUtil.AUTH_HEADER, signInResponse.getAccessToken())
                    .content(objectMapper.writeValueAsString(changeRoleRequest)))
            .andExpect(status().isForbidden());
    TestUtil.checkErrorMessages(expectedForbiddenResponse, resultActions);
  }

  @ParameterizedTest
  @CsvSource({
    "lock, user",
    "unlock, locked",
    "deactivate, forUpdate1",
    "activate, notActivated",
  })
  void lockUnlockDeactivateActivateUsersShouldThrowUnauthorized_userNotSignedIn(
      String path, String username) throws Exception {
    var adminRequest = new AdminRequest(Set.of(username));
    var resultActions =
        mockMvc
            .perform(
                put(Path.ADMIN + "/users/" + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(adminRequest)))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @ParameterizedTest
  @CsvSource({
    "lock, user",
    "unlock, locked",
    "deactivate, forUpdate1",
    "activate, notActivated",
  })
  void lockUnlockDeactivateActivateUsersShouldThrowForbidden_signedInUserIsNotAdmin(
      String path, String username) throws Exception {
    var signInResponse =
        TestUtil.signIn(mockMvc, objectMapper, new SignInRequest("user", "qwerty123"));
    var adminRequest = new AdminRequest(Set.of(username));
    var resultActions =
        mockMvc
            .perform(
                put(Path.ADMIN + "/users/" + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(AuthUtil.AUTH_HEADER, signInResponse.getAccessToken())
                    .content(objectMapper.writeValueAsString(adminRequest)))
            .andExpect(status().isForbidden());
    TestUtil.checkErrorMessages(expectedForbiddenResponse, resultActions);
  }

  @Test
  void deleteUsersShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var adminRequest = new AdminRequest(Set.of("forUpdate2"));
    var resultActions =
        mockMvc
            .perform(
                delete(Path.ADMIN + "/users/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(adminRequest)))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @Test
  void deleteUsersShouldThrowForbidden_signedInUserIsNotAdmin() throws Exception {
    var signInResponse =
        TestUtil.signIn(mockMvc, objectMapper, new SignInRequest("user", "qwerty123"));
    var adminRequest = new AdminRequest(Set.of("forUpdate2"));
    var resultActions =
        mockMvc
            .perform(
                delete(Path.ADMIN + "/users/delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(AuthUtil.AUTH_HEADER, signInResponse.getAccessToken())
                    .content(objectMapper.writeValueAsString(adminRequest)))
            .andExpect(status().isForbidden());
    TestUtil.checkErrorMessages(expectedForbiddenResponse, resultActions);
  }

  @Test
  void receiveSignedInUserShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var resultActions =
        mockMvc
            .perform(get(Path.AUTH + "/signed-in-user").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @Test
  void signOutShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var resultActions =
        mockMvc
            .perform(post(Path.AUTH + "/sign-out").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }

  @Test
  void signOutFromAllDevicesShouldThrowUnauthorized_userNotSignedIn() throws Exception {
    var resultActions =
        mockMvc
            .perform(
                post(Path.AUTH + "/sign-out-from-all-devices")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    TestUtil.checkErrorMessages(expectedUnauthorizedResponse, resultActions);
  }
}
