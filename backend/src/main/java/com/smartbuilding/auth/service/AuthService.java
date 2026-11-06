package com.smartbuilding.auth.service;

import com.smartbuilding.auth.dto.AuthResponse;
import com.smartbuilding.auth.dto.LoginRequest;
import com.smartbuilding.auth.dto.RegisterRequest;
import com.smartbuilding.user.dto.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);
}
