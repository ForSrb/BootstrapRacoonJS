package org.bootstrapbugz.api.user.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.bootstrapbugz.api.shared.config.DatabaseContainers;
import org.bootstrapbugz.api.user.model.Role;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.bootstrapbugz.api.user.model.User;
import org.bootstrapbugz.api.user.repository.RoleRepository;
import org.bootstrapbugz.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT extends DatabaseContainers {
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;

  @BeforeEach
  void setUp() {
    final var adminRole = roleRepository.save(new Role(RoleName.ADMIN));
    final var userRole = roleRepository.save(new Role(RoleName.USER));
    userRepository.save(
        new User()
            .setFirstName("Admin")
            .setLastName("Admin")
            .setUsername("admin")
            .setEmail("admin@localhost")
            .setPassword("password")
            .setRoles(Set.of(adminRole, userRole)));
    userRepository.save(
        new User()
            .setFirstName("Test")
            .setLastName("Test")
            .setUsername("test")
            .setEmail("test@localhost")
            .setPassword("password")
            .setRoles(Set.of(userRole)));
  }

  @Test
  void findAllWithRoles() {
    assertThat(userRepository.findAllWithRoles()).hasSize(2);
  }

  @Test
  void findAllByUsernameIn() {
    assertThat(userRepository.findAllByUsernameIn(Set.of("admin", "test"))).hasSize(2);
  }

  @Test
  void findByEmail() {
    assertThat(userRepository.findByEmail("admin@localhost")).isPresent();
  }

  @Test
  void findByUsername() {
    assertThat(userRepository.findByUsername("admin")).isPresent();
  }

  @Test
  void findByUsernameWithRoles() {
    final var user = userRepository.findByUsernameWithRoles("admin");
    assertThat(user).isPresent();
    user.ifPresent(value -> assertThat(value.getRoles()).hasSize(2));
  }

  @Test
  void findByUsernameOrEmail() {
    assertThat(userRepository.findByUsernameOrEmail("admin", "admin@localhost")).isPresent();
  }

  @Test
  void existsByEmail() {
    assertThat(userRepository.existsByEmail("admin@localhost")).isTrue();
  }

  @Test
  void existsByUsername() {
    assertThat(userRepository.existsByUsername("admin")).isTrue();
  }
}
