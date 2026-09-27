package com.digisecurebank.payment_service.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentOrderResponse {

    private String paymentId;
    private String razorPayOrderId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String razorPayKeyId;

}
