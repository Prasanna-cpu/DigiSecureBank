package com.digisecurebank.auth_service.request;


import com.digisecurebank.auth_service.enums.UserRoles;
import com.digisecurebank.auth_service.validation.PasswordMatches;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches
public class RegisterRequest {

    @NotBlank(message = "fullName must not be blank")
    @Size(min = 3, message = "Fullname must be at least 3 characters long")
    private String fullName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "password is required")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "confirmPassword is required")
    private String confirmPassword;

    private UserRoles role;

}
