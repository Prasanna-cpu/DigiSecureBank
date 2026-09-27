package com.digisecurebank.transaction_service.entity;


import com.digisecurebank.transaction_service.enums.TransactionStatus;
import com.digisecurebank.transaction_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transactions", check = @CheckConstraint(constraint = "sender_account_number != receiver_account_number"))
public class Transaction extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sender_account_number", nullable = false)
    private String senderAccountNumber;

    @Column(name = "receiver_account_number", nullable = false)
    private String receiverAccountNumber;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2, check = @CheckConstraint(constraint = "amount > 0"))
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "reference_number")
    private String referenceNumber;

}
