package org.bootstrapbugz.api.admin.service.impl;

import java.util.stream.Collectors;
import org.bootstrapbugz.api.admin.payload.request.AdminRequest;
import org.bootstrapbugz.api.admin.payload.request.UpdateRoleRequest;
import org.bootstrapbugz.api.admin.service.AdminService;
import org.bootstrapbugz.api.auth.jwt.service.AccessTokenService;
import org.bootstrapbugz.api.auth.jwt.service.RefreshTokenService;
import org.bootstrapbugz.api.user.model.Role;
import org.bootstrapbugz.api.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {
  private final UserRepository userRepository;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;

  public AdminServiceImpl(
      UserRepository userRepository,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService) {
    this.userRepository = userRepository;
    this.accessTokenService = accessTokenService;
    this.refreshTokenService = refreshTokenService;
  }

  @Override
  public void activate(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(user -> user.setActivated(true));
    userRepository.saveAll(users);
  }

  @Override
  public void deactivate(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          user.setActivated(false);
          accessTokenService.invalidateAllByUser(user.getId());
          refreshTokenService.deleteAllByUser(user.getId());
        });
    userRepository.saveAll(users);
  }

  @Override
  public void unlock(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(user -> user.setNonLocked(true));
    userRepository.saveAll(users);
  }

  @Override
  public void lock(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          user.setNonLocked(false);
          accessTokenService.invalidateAllByUser(user.getId());
          refreshTokenService.deleteAllByUser(user.getId());
        });
    userRepository.saveAll(users);
  }

  @Override
  public void updateRole(UpdateRoleRequest updateRoleRequest) {
    final var users = userRepository.findAllByUsernameIn(updateRoleRequest.getUsernames());
    users.forEach(
        user -> {
          final var roles =
              updateRoleRequest.getRoleNames().stream().map(Role::new).collect(Collectors.toSet());
          user.setRoles(roles);
          accessTokenService.invalidateAllByUser(user.getId());
          refreshTokenService.deleteAllByUser(user.getId());
        });
    userRepository.saveAll(users);
  }

  @Override
  @Transactional
  public void delete(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          accessTokenService.invalidateAllByUser(user.getId());
          refreshTokenService.deleteAllByUser(user.getId());
          userRepository.delete(user);
        });
  }
}