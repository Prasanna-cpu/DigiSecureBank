package com.digisecurebank.payment_service.entity;


import com.digisecurebank.payment_service.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.message.StringFormattedMessage;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "razor_pay_order_id", unique = true)
    private String razorPayOrderId;

    @Column(name = "razor_pay_payment_id", unique = true)
    private String razorPayPaymentId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false, check = @CheckConstraint(constraint = "amount > 0"), precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "description")
    private String description;

    @Column(name = "failure_reason")
    private String failureReason;


}
