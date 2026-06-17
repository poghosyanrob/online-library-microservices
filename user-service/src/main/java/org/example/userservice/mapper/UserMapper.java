package org.example.userservice.mapper;

import org.example.userservice.model.User;
import org.example.userservice.model.dto.RegisterRequest;
import org.example.userservice.model.dto.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(RegisterRequest request);

    UserDto toDto(User user);
}
