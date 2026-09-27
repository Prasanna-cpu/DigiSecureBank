package com.digisecurebank.auth_service.controller;


import com.digisecurebank.auth_service.request.LoginRequest;
import com.digisecurebank.auth_service.request.RegisterRequest;
import com.digisecurebank.auth_service.response.ApiResponse;
import com.digisecurebank.auth_service.response.LoginResponse;
import com.digisecurebank.auth_service.response.RegisterResponse;
import com.digisecurebank.auth_service.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerHandler(
            @Valid @RequestBody RegisterRequest request
    ){
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse(
                       response,
                       "User Created",
                       HttpStatus.CREATED,
                       HttpStatus.CREATED.value()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginHandler(
            @Valid @RequestBody LoginRequest request
    ){
        LoginResponse response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                       response,
                       "User Logged In",
                       HttpStatus.OK,
                       HttpStatus.OK.value()
                )
        );
    }

}
