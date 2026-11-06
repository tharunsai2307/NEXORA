package com.smartbuilding.user.service;

import com.smartbuilding.user.dto.UserResponse;
import com.smartbuilding.user.model.Role;

import java.util.List;

public interface UserService {
    List<UserResponse> listUsers(String query, Role role, Boolean active);
    UserResponse getUserById(String id);
    UserResponse updateUserRole(String id, Role newRole, String adminUserId);
    UserResponse updateUserStatus(String id, boolean active, String adminUserId);
}
