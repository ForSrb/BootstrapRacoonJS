package org.bootstrapbugz.api.auth.util;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.bootstrapbugz.api.auth.security.user.details.UserPrincipal;
import org.bootstrapbugz.api.user.model.Role;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.bootstrapbugz.api.user.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
  public static final String AUTH_HEADER = "Authorization";

  private AuthUtil() {}

  public static boolean isSignedIn() {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    return !auth.getPrincipal().equals("anonymousUser");
  }

  public static boolean isAdminSignedIn() {
    if (!isSignedIn()) return false;
    User user = findSignedInUser();
    return user.getRoles().contains(new Role(RoleName.ADMIN));
  }

  public static User findSignedInUser() {
    final var auth = SecurityContextHolder.getContext().getAuthentication();
    return userPrincipalToUser((UserPrincipal) auth.getPrincipal());
  }

  public static User userPrincipalToUser(UserPrincipal userPrincipal) {
    return new User()
        .setId(userPrincipal.getId())
        .setFirstName(userPrincipal.getFirstName())
        .setLastName(userPrincipal.getLastName())
        .setUsername(userPrincipal.getUsername())
        .setEmail(userPrincipal.getEmail())
        .setPassword(userPrincipal.getPassword())
        .setActivated(userPrincipal.isEnabled())
        .setNonLocked(userPrincipal.isAccountNonLocked())
        .setRoles(
            userPrincipal.getAuthorities().stream()
                .map(authority -> new Role(RoleName.valueOf(authority.getAuthority())))
                .collect(Collectors.toSet()));
  }

  public static String getUserIpAddress(HttpServletRequest request) {
    String ipAddress = request.getHeader("x-forwarded-for");
    if (ipAddress == null || ipAddress.isEmpty()) ipAddress = request.getRemoteAddr();
    return ipAddress;
  }

  public static String getAccessTokenFromRequest(HttpServletRequest request) {
    return request.getHeader(AUTH_HEADER);
  }
}