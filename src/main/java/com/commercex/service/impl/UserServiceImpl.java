package com.commercex.service.impl;

import com.commercex.dto.RegisterRequest;
import com.commercex.dto.UserResponse;
import com.commercex.entity.Role;
import com.commercex.entity.User;
import com.commercex.exception.ResourceNotFoundException;
import com.commercex.exception.UserAlreadyExistsException;
import com.commercex.mapper.UserMapper;
import com.commercex.repository.UserRepository;
import com.commercex.service.RoleService;
import com.commercex.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder; // Injected BCrypt encoder

    @Override
    @Transactional
    public UserResponse createUser(RegisterRequest request) {
        if (existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists.");
        }

        User user = userMapper.toEntity(request);
        
        // Hash the password securely using BCrypt before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        Role customerRole = roleService.getDefaultCustomerRole();
        user.setRoles(Set.of(customerRole));
        
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public User getCurrentUser() {
        throw new UnsupportedOperationException("getCurrentUser requires Spring Security Context (Step 4)");
    }
}
