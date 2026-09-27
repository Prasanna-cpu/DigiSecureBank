package com.digisecurebank.transaction_service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DistinctAccountNumbersValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistinctAccountNumbers {
    String message() default "Sender account number must be different from receiver account number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
