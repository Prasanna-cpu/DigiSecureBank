package com.digisecurebank.auth_service.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String accessToken;
    private String refreshToken;
    private Object data;
}
