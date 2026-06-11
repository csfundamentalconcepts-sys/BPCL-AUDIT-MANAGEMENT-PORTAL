package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String username);

    Boolean existsByUserName(String username);

    List<User> findByRole_RoleName(AppRole roleName);

    @Query(
            value = """
                SELECT u.*
                FROM users u
                INNER JOIN user_assignments ua
                ON u.id = ua.user_id
                WHERE ua.assigned_to_user_id = :userId
                 """,
            nativeQuery = true
    )
    List<User> getChildUsers(@Param("userId")Long userId);
}

