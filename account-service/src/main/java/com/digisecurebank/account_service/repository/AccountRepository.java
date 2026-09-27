package com.digisecurebank.account_service.repository;

import com.digisecurebank.account_service.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByEmail(String email);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.email = :email")
    Optional<Account> findByEmail(String email);

    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumber(String accountNumber);

}
