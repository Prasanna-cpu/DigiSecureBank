package com.digisecurebank.auth_service.repository;

import com.digisecurebank.auth_service.entity.Users;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {


    boolean existsByEmail(String email);

    @Query("SELECT u FROM Users u WHERE u.email = :email")
    Optional<Users> findByEmail(String email);

}
