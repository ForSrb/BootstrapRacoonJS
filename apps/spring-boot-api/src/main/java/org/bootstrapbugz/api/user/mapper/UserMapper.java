package org.bootstrapbugz.api.user.mapper;

import java.util.List;

import org.bootstrapbugz.api.user.model.User;
import org.bootstrapbugz.api.user.response.UserResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserMapper {
  UserResponse userToUserResponse(User user);

  List<UserResponse> usersToUserResponses(List<User> users);
}
