package dev.stiebo.app.services;

import dev.stiebo.app.configuration.RoleName;
import dev.stiebo.app.data.RoleRepository;
import dev.stiebo.app.data.User;
import dev.stiebo.app.data.UserRepository;

import java.util.List;

import dev.stiebo.app.dtos.UserDto;
import dev.stiebo.app.mappers.UserMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper mapper, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
        this.encoder = encoder;
    }

    @Transactional
    public void createUser (UserDto userDto) throws RuntimeException {
        if (userRepository.existsByUsernameIgnoreCase(userDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        User newUser = new User()
                .setName(userDto.getName())
                .setUsername(userDto.getUsername())
                .setPassword(encoder.encode(userDto.getPassword()))
                .setRole(roleRepository.findByName(userDto.getRoleName()));
        userRepository.save(newUser);
    }

    @Transactional
    public void deleteUser (UserDto userDto) {
        userRepository.delete(userRepository.findByUsername(userDto.getUsername())
                .orElseThrow(() -> new RuntimeException("Username not found")));
    }

    public List<UserDto> listWithFilter(String filter, Pageable pageable) {
        if (filter == null || filter.isEmpty()) {
            return userRepository.findAll(pageable)
                    .stream()
                    .map(mapper::toDto)
                    .toList();
        } else {
            return userRepository.findByNameOrUsernameOrRoleNameContainingIgnoreCase(filter, pageable)
                    .stream()
                    .map(mapper::toDto)
                    .toList();
        }
    }

    public int countWithFilter(String filter) {
        if (filter == null || filter.isEmpty()) {
            return (int) userRepository.count();
        } else {
            return (int) userRepository.countByNameOrUsernameOrRoleNameContainingIgnoreCase(filter);
        }
    }

}
