package dev.stiebo.app.mappers;

import dev.stiebo.app.data.RoleRepository;
import dev.stiebo.app.data.User;
import dev.stiebo.app.dtos.UserDto;
import org.springframework.stereotype.Service;

@Service
public class UserMapper {
    private final RoleRepository roleRepository;

    public UserMapper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public User toUser(UserDto dto) {
        return new User()
                .setName(dto.name())
                .setUsername(dto.username())
                .setPassword(dto.password())
                .setRole(roleRepository.findByName(dto.roleName()));
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getName(), user.getUsername(), user.getPassword(), user.getRole().getName());
    }
}
