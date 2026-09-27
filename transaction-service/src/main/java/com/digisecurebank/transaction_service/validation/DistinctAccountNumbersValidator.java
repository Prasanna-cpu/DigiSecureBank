package com.digisecurebank.transaction_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DistinctAccountNumbersValidator implements ConstraintValidator<DistinctAccountNumbers, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            String senderAccountNumber = (String) value.getClass().getMethod("getSenderAccountNumber").invoke(value);
            String receiverAccountNumber = (String) value.getClass().getMethod("getReceiverAccountNumber").invoke(value);

            return senderAccountNumber == null || !senderAccountNumber.equals(receiverAccountNumber);
        } catch (Exception e) {
            return false;
        }
    }
}
