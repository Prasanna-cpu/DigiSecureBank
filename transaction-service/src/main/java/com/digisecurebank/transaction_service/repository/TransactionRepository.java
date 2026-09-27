package com.digisecurebank.transaction_service.repository;

import com.digisecurebank.transaction_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("select distinct t from Transaction t where t.senderAccountNumber = :accountNumber or t.receiverAccountNumber = :accountNumber order by t.createdAt desc")
    List<Transaction> findAllBySenderAccountNumberOrReceiverAccountNumber (String accountNumber);

}
