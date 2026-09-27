package com.digisecurebank.account_service.dto;

import com.digisecurebank.account_service.enums.AccountStatus;
import com.digisecurebank.account_service.enums.AccountType;
import jakarta.persistence.*;
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
public class AccountDTO {

    private UUID id;

    private String accountNumber;

    private String accountHolderName;

    private String email;

    private String phone;

    private AccountType accountType;

    private BigDecimal balance;

    private AccountStatus status;

//    private BigDecimal interest;

    private BigDecimal dailyTransactionLimit;


}
