package com.commercex.service;

import com.commercex.dto.RegisterRequest;
import com.commercex.dto.UserResponse;
import com.commercex.entity.User;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(RegisterRequest request);
    User findByEmail(String email);
    boolean existsByEmail(String email);
    UserResponse getUserById(UUID id);
    User getCurrentUser();
    void requestPasswordReset(String email);
}
