package com.shopping.mapper;

import com.shopping.dto.response.auth.UserSummaryResponse;
import com.shopping.entity.Role;
import com.shopping.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToStringSet")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserSummaryResponse toUserSummaryResponse(User user);

    @Named("mapRolesToStringSet")
    default Set<String> mapRolesToStringSet(Set<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(role -> role.getRoleCode().name())
                .collect(Collectors.toSet());
    }
}
