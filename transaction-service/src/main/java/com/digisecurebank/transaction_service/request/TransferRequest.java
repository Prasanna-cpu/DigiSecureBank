package com.digisecurebank.transaction_service.request;

import com.digisecurebank.transaction_service.validation.DistinctAccountNumbers;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@DistinctAccountNumbers
public class TransferRequest {

    @NotBlank(message = "Sender account number cannot be blank")
    private String senderAccountNumber;

    @NotBlank(message = "Receiver account number cannot be blank")
    private String receiverAccountNumber;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;
}
