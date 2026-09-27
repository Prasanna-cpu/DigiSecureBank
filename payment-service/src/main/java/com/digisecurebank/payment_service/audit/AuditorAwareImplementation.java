package com.digisecurebank.payment_service.audit;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component("auditorAwareImplementation")
public class AuditorAwareImplementation implements AuditorAware<String> {
    @Override
    public @NonNull Optional<String> getCurrentAuditor() {
        return Optional.of("PAYMENT-SERVICE");
    }
}
