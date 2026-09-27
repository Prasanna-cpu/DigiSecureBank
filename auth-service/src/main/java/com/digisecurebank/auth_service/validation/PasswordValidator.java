package com.digisecurebank.auth_service.validation;

import com.digisecurebank.auth_service.request.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.type.TrueFalseConverter;

public class PasswordValidator implements ConstraintValidator<PasswordMatches, RegisterRequest> {
    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(RegisterRequest registerRequest, ConstraintValidatorContext constraintValidatorContext) {
        if(registerRequest == null){
            return true;
        }
        if(registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null){
            return true;
        }
        return registerRequest.getPassword().equals(registerRequest.getConfirmPassword());
    }
}
