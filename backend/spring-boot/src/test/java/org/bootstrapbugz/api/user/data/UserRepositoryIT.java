package org.bootstrapbugz.api.user.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.bootstrapbugz.api.shared.config.DatabaseContainers;
import org.bootstrapbugz.api.user.model.Role;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.bootstrapbugz.api.user.model.User;
import org.bootstrapbugz.api.user.repository.RoleRepository;
import org.bootstrapbugz.api.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT extends DatabaseContainers {
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;

  @BeforeEach
  void setUp() {
    final var adminRole = roleRepository.save(new Role(RoleName.ADMIN));
    final var userRole = roleRepository.save(new Role(RoleName.USER));
    userRepository.save(
        User.builder()
            .firstName("Admin")
            .lastName("Admin")
            .username("admin")
            .email("admin@localhost")
            .password("password")
            .roles(Set.of(adminRole, userRole))
            .build());
    userRepository.save(
        User.builder()
            .firstName("User")
            .lastName("User")
            .username("user")
            .email("user@localhost")
            .password("password")
            .roles(Set.of(userRole))
            .build());
  }

  @AfterEach
  void cleanUp() {
    userRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  void findAllWithRoles() {
    assertThat(userRepository.findAllWithRoles()).hasSize(2);
  }

  @Test
  void findByIdWithRoles() {
    final var id = userRepository.findByUsername("admin").orElseThrow().getId();
    final var user = userRepository.findByIdWithRoles(id).orElseThrow();
    assertThat(user.getUsername()).isEqualTo("admin");
    assertThat(user.getRoles()).hasSize(2);
  }

  @Test
  void findByUsername() {
    assertThat(userRepository.findByUsername("admin")).isPresent();
  }

  @Test
  void findByUsernameWithRoles() {
    final var user = userRepository.findByUsernameWithRoles("user").orElseThrow();
    assertThat(user.getUsername()).isEqualTo("user");
    assertThat(user.getRoles()).hasSize(1);
  }

  @Test
  void findByEmail() {
    assertThat(userRepository.findByEmail("admin@localhost")).isPresent();
  }

  @Test
  void findByUsernameOrEmail() {
    assertThat(userRepository.findByUsernameOrEmail("admin", "admin@localhost")).isPresent();
  }

  @Test
  void existsByUsername() {
    assertThat(userRepository.existsByUsername("admin")).isTrue();
  }

  @Test
  void existsByEmail() {
    assertThat(userRepository.existsByEmail("admin@localhost")).isTrue();
  }
}
