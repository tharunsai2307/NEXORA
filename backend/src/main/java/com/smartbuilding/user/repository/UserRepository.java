package com.smartbuilding.user.repository;

import com.smartbuilding.user.model.Role;
import com.smartbuilding.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRoleAndActive(Role role, boolean active);

    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:active IS NULL OR u.active = :active) AND " +
           "(:query IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY u.createdAt DESC")
    List<User> searchUsers(@Param("query") String query, @Param("role") Role role, @Param("active") Boolean active);
}
