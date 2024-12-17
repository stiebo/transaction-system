package dev.stiebo.app.dtos;

import dev.stiebo.app.configuration.RoleName;

public record UserDto(
        String name,
        String username,
        String password,
        RoleName roleName
) {
}
