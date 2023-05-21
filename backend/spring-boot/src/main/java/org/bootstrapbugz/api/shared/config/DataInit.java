package org.bootstrapbugz.api.shared.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.bootstrapbugz.api.user.model.Role;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.bootstrapbugz.api.user.model.User;
import org.bootstrapbugz.api.user.repository.RoleRepository;
import org.bootstrapbugz.api.user.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile({"dev", "test"})
@Component
public class DataInit implements ApplicationRunner {
  private static final String PASSWORD = "qwerty123";
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder bCryptPasswordEncoder;
  private final Environment environment;
  private final Role userRole = new Role(RoleName.USER);
  private final Role adminRole = new Role(RoleName.ADMIN);

  public DataInit(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder bCryptPasswordEncoder,
      Environment environment) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.environment = environment;
  }

  @Override
  public void run(ApplicationArguments args) {
    saveRoles();
    saveUsers();
  }

  private void saveRoles() {
    roleRepository.saveAll(List.of(userRole, adminRole));
  }

  private void saveUsers() {
    userRepository.saveAll(
        List.of(
            User.builder()
                .firstName("Admin")
                .lastName("Admin")
                .username("admin")
                .email("admin@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(true)
                .nonLocked(true)
                .roles(Set.of(userRole, adminRole))
                .build(),
            User.builder()
                .firstName("User")
                .lastName("User")
                .username("user")
                .email("user@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(true)
                .nonLocked(true)
                .roles(Collections.singleton(userRole))
                .build()));
    if (environment.getActiveProfiles()[0].equals("dev")) devUsers();
    else if (environment.getActiveProfiles()[0].equals("test")) testUsers();
  }

  private void devUsers() {
    userRepository.saveAll(
        List.of(
            User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .email("john.doe@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(true)
                .nonLocked(true)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .username("jane.doe")
                .email("jane.doe@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(true)
                .nonLocked(true)
                .roles(Collections.singleton(userRole))
                .build()));
  }

  private void testUsers() {
    userRepository.saveAll(
        List.of(
            User.builder()
                .firstName("Deactivated")
                .lastName("Deactivated")
                .username("deactivated")
                .email("deactivated@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(false)
                .nonLocked(true)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .firstName("Locked")
                .lastName("Locked")
                .username("locked")
                .email("locked@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(true)
                .nonLocked(false)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .firstName("Update1")
                .lastName("Update1")
                .username("update1")
                .email("update1@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(true)
                .nonLocked(true)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .firstName("Update2")
                .lastName("Update2")
                .username("update2")
                .email("update2@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(true)
                .nonLocked(true)
                .roles(Collections.singleton(userRole))
                .build(),
            User.builder()
                .firstName("Update3")
                .lastName("Update3")
                .username("update3")
                .email("update3@bootstrapbugz.com")
                .password(bCryptPasswordEncoder.encode(PASSWORD))
                .activated(true)
                .nonLocked(true)
                .roles(Collections.singleton(userRole))
                .build()));
  }
}
