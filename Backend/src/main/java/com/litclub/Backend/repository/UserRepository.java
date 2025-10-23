package com.litclub.Backend.repository;

import com.litclub.Backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
        select distinct u
        from User u
        left join fetch u.globalRoles gr
        left join fetch u.memberships m
        left join fetch m.club c
        left join fetch m.roles r
        where u.username = :username
        """)
    Optional<User> findByUsernameWithMembershipsAndRoles(@Param("username") String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
}

