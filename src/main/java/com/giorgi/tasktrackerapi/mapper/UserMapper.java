package com.giorgi.tasktrackerapi.mapper;

import com.giorgi.tasktrackerapi.dto.user.UserResponseDto;
import com.giorgi.tasktrackerapi.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toResponseDto(User user);
}
