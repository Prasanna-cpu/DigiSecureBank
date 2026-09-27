package com.digisecurebank.transaction_service.dto;

import com.digisecurebank.transaction_service.enums.TransactionStatus;
import com.digisecurebank.transaction_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    private UUID id;

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private TransactionType type;

    private TransactionStatus status;

    private String description;

    private String failureReason;

    private String referenceNumber;

}
