package com.smartbuilding.user.service;

import com.smartbuilding.audit.service.AuditService;
import com.smartbuilding.common.ServiceException;
import com.smartbuilding.user.dto.UserResponse;
import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.model.User;
import com.smartbuilding.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public UserServiceImpl(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(String query, Role role, Boolean active) {
        String trimmedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        return userRepository.searchUsers(trimmedQuery, role, active)
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("User not found with ID: " + id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(String id, Role newRole, String adminUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("User not found with ID: " + id));

        if (user.getRole() == Role.SYSTEM_ADMIN && newRole != Role.SYSTEM_ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndActive(Role.SYSTEM_ADMIN, true);
            if (activeAdminCount <= 1 && user.isActive()) {
                throw ServiceException.badRequest("Cannot demote the last active System Administrator");
            }
        }

        Role oldRole = user.getRole();
        user.setRole(newRole);
        User savedUser = userRepository.save(user);

        auditService.log(
                adminUserId,
                "USER_ROLE_CHANGED",
                "USER",
                user.getId(),
                "Role changed from " + oldRole + " to " + newRole + " for user " + user.getEmail()
        );

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(String id, boolean active, String adminUserId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> ServiceException.notFound("User not found with ID: " + id));

        if (!active && user.getRole() == Role.SYSTEM_ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndActive(Role.SYSTEM_ADMIN, true);
            if (activeAdminCount <= 1 && user.isActive()) {
                throw ServiceException.badRequest("Cannot deactivate the last active System Administrator");
            }
        }

        boolean oldStatus = user.isActive();
        user.setActive(active);
        User savedUser = userRepository.save(user);

        auditService.log(
                adminUserId,
                "USER_STATUS_CHANGED",
                "USER",
                user.getId(),
                "Status changed from " + (oldStatus ? "ACTIVE" : "INACTIVE") + " to " + (active ? "ACTIVE" : "INACTIVE") + " for user " + user.getEmail()
        );

        return UserResponse.fromEntity(savedUser);
    }
}

