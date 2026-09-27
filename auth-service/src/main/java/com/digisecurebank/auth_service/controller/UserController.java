package com.digisecurebank.auth_service.controller;

import com.digisecurebank.auth_service.dto.UsersDTO;
import com.digisecurebank.auth_service.response.ApiResponse;
import com.digisecurebank.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getUserFromAuthHandler(
            @RequestHeader("Authorization") String jwt
    ){
        UsersDTO userDTO = userService.getUserFromAuth(jwt);
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        userDTO,
                        "User Retrieved",
                        HttpStatus.OK,
                        HttpStatus.OK.value()
                )
        );
    }

}
