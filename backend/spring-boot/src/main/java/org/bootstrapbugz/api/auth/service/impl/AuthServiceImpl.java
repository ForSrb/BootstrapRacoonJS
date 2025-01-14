package org.bootstrapbugz.api.auth.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import org.bootstrapbugz.api.auth.jwt.service.AccessTokenService;
import org.bootstrapbugz.api.auth.jwt.service.RefreshTokenService;
import org.bootstrapbugz.api.auth.jwt.service.ResetPasswordTokenService;
import org.bootstrapbugz.api.auth.jwt.service.VerificationTokenService;
import org.bootstrapbugz.api.auth.jwt.util.JwtUtil;
import org.bootstrapbugz.api.auth.payload.dto.AuthTokensDTO;
import org.bootstrapbugz.api.auth.payload.request.AuthTokensRequest;
import org.bootstrapbugz.api.auth.payload.request.ForgotPasswordRequest;
import org.bootstrapbugz.api.auth.payload.request.RegisterUserRequest;
import org.bootstrapbugz.api.auth.payload.request.ResetPasswordRequest;
import org.bootstrapbugz.api.auth.payload.request.VerificationEmailRequest;
import org.bootstrapbugz.api.auth.payload.request.VerifyEmailRequest;
import org.bootstrapbugz.api.auth.service.AuthService;
import org.bootstrapbugz.api.auth.util.AuthUtil;
import org.bootstrapbugz.api.shared.error.exception.BadRequestException;
import org.bootstrapbugz.api.shared.error.exception.ConflictException;
import org.bootstrapbugz.api.shared.error.exception.ResourceNotFoundException;
import org.bootstrapbugz.api.shared.error.exception.UnauthorizedException;
import org.bootstrapbugz.api.shared.message.service.MessageService;
import org.bootstrapbugz.api.user.mapper.UserMapper;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.bootstrapbugz.api.user.model.User;
import org.bootstrapbugz.api.user.payload.dto.UserDTO;
import org.bootstrapbugz.api.user.repository.RoleRepository;
import org.bootstrapbugz.api.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final MessageService messageService;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final AuthenticationManager authenticationManager;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final VerificationTokenService verificationTokenService;
  private final ResetPasswordTokenService resetPasswordTokenService;

  public AuthServiceImpl(
      UserRepository userRepository,
      RoleRepository roleRepository,
      MessageService messageService,
      PasswordEncoder bCryptPasswordEncoder,
      AuthenticationManager authenticationManager,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService,
      VerificationTokenService verificationTokenService,
      ResetPasswordTokenService resetPasswordTokenService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.messageService = messageService;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.authenticationManager = authenticationManager;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
    this.verificationTokenService = verificationTokenService;
    this.resetPasswordTokenService = resetPasswordTokenService;
  }

  @Override
  public UserDTO register(RegisterUserRequest registerUserRequest) {
    if (userRepository.existsByUsername(registerUserRequest.username()))
      throw new ConflictException(messageService.getMessage("username.exists"));
    if (userRepository.existsByEmail(registerUserRequest.email()))
      throw new ConflictException(messageService.getMessage("email.exists"));
    final var user = userRepository.save(createUser(registerUserRequest));
    final var token = verificationTokenService.create(user.getId());
    verificationTokenService.sendToEmail(user, token);
    return UserMapper.INSTANCE.userToProfileUserDTO(user);
  }

  @Override
  public AuthTokensDTO authenticate(AuthTokensRequest authTokensRequest, String ipAddress) {
    final var auth =
        new UsernamePasswordAuthenticationToken(
            authTokensRequest.usernameOrEmail(), authTokensRequest.password(), new ArrayList<>());
    authenticationManager.authenticate(auth);

    final var user =
        userRepository
            .findByUsernameWithRoles(auth.getName())
            .orElseThrow(
                () -> new UnauthorizedException(messageService.getMessage("auth.invalid")));
    final var roleDTOs = UserMapper.INSTANCE.rolesToRoleDTOs(user.getRoles());
    final var accessToken = accessTokenService.create(user.getId(), roleDTOs);
    final var refreshToken =
        refreshTokenService
            .findByUserIdAndIpAddress(user.getId(), ipAddress)
            .orElse(refreshTokenService.create(user.getId(), roleDTOs, ipAddress));
    return new AuthTokensDTO(accessToken, refreshToken);
  }

  private User createUser(RegisterUserRequest registerUserRequest) {
    final var roles =
        roleRepository
            .findByName(RoleName.USER)
            .orElseThrow(() -> new RuntimeException(messageService.getMessage("role.notFound")));
    return User.builder()
        .firstName(registerUserRequest.firstName())
        .lastName(registerUserRequest.lastName())
        .username(registerUserRequest.username())
        .email(registerUserRequest.email())
        .password(bCryptPasswordEncoder.encode(registerUserRequest.password()))
        .roles(Collections.singleton(roles))
        .build();
  }

  @Override
  public void deleteTokens(String accessToken, String ipAddress) {
    if (!AuthUtil.isSignedIn()) return;
    final var id = AuthUtil.findSignedInUser().getId();
    refreshTokenService.deleteByUserIdAndIpAddress(id, ipAddress);
    accessTokenService.invalidate(accessToken);
  }

  @Override
  public void deleteTokensOnAllDevices() {
    if (!AuthUtil.isSignedIn()) return;
    final var id = AuthUtil.findSignedInUser().getId();
    refreshTokenService.deleteAllByUserId(id);
    accessTokenService.invalidateAllByUserId(id);
  }

  @Override
  public AuthTokensDTO refreshTokens(String refreshToken, String ipAddress) {
    refreshTokenService.check(refreshToken);
    final var userId = JwtUtil.getUserId(refreshToken);
    final var roleDTOs = JwtUtil.getRoleDTOs(refreshToken);
    refreshTokenService.delete(refreshToken);
    final var newAccessToken = accessTokenService.create(userId, roleDTOs);
    final var newRefreshToken = refreshTokenService.create(userId, roleDTOs, ipAddress);
    return new AuthTokensDTO(newAccessToken, newRefreshToken);
  }

  @Override
  public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
    final var user =
        userRepository
            .findByEmail(forgotPasswordRequest.email())
            .orElseThrow(
                () -> new ResourceNotFoundException(messageService.getMessage("user.notFound")));
    final var token = resetPasswordTokenService.create(user.getId());
    resetPasswordTokenService.sendToEmail(user, token);
  }

  @Override
  public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
    final var user =
        userRepository
            .findById(JwtUtil.getUserId(resetPasswordRequest.token()))
            .orElseThrow(
                () -> new BadRequestException("token", messageService.getMessage("token.invalid")));
    resetPasswordTokenService.check(resetPasswordRequest.token());
    user.setPassword(bCryptPasswordEncoder.encode(resetPasswordRequest.password()));
    accessTokenService.invalidateAllByUserId(user.getId());
    refreshTokenService.deleteAllByUserId(user.getId());
    userRepository.save(user);
  }

  @Override
  public void sendVerificationMail(VerificationEmailRequest request) {
    final var user =
        userRepository
            .findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
            .orElseThrow(
                () -> new ResourceNotFoundException(messageService.getMessage("user.notFound")));
    if (Boolean.TRUE.equals(user.getActive()))
      throw new ConflictException(messageService.getMessage("user.active"));
    final var token = verificationTokenService.create(user.getId());
    verificationTokenService.sendToEmail(user, token);
  }

  @Override
  public void verifyEmail(VerifyEmailRequest verifyEmailRequest) {
    final var user =
        userRepository
            .findById(JwtUtil.getUserId(verifyEmailRequest.token()))
            .orElseThrow(
                () -> new BadRequestException("token", messageService.getMessage("token.invalid")));
    if (Boolean.TRUE.equals(user.getActive()))
      throw new ConflictException(messageService.getMessage("user.active"));
    verificationTokenService.check(verifyEmailRequest.token());
    user.setActive(true);
    userRepository.save(user);
  }
}
