package dev.stiebo.app.dtos;

import dev.stiebo.app.configuration.RoleName;

public class UserDto {
    private String name;
    private String username;
    private String password;
    private RoleName roleName;

    public UserDto() {
    }

    public UserDto(String name, String username, String password, RoleName roleName) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.roleName = roleName;
    }

    public String getName() {
        return name;
    }

    public UserDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserDto setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserDto setPassword(String password) {
        this.password = password;
        return this;
    }

    public RoleName getRoleName() {
        return roleName;
    }

    public UserDto setRoleName(RoleName roleName) {
        this.roleName = roleName;
        return this;
    }
}
