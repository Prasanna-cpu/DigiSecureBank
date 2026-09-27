package com.digisecurebank.transaction_service.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInitiatedEvent {

    private UUID transactionId;

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private String description;
}
