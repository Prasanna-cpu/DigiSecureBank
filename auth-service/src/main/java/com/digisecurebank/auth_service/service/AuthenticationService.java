package com.digisecurebank.auth_service.service;


import com.digisecurebank.auth_service.dto.UsersDTO;
import com.digisecurebank.auth_service.entity.Users;
import com.digisecurebank.auth_service.enums.UserRoles;
import com.digisecurebank.auth_service.exception.BadRequestException;
import com.digisecurebank.auth_service.exception.ConflictingResourcesException;
import com.digisecurebank.auth_service.exception.UnauthorizedAccessException;
import com.digisecurebank.auth_service.jwt.JWTProvider;
import com.digisecurebank.auth_service.mapper.UserMapper;
import com.digisecurebank.auth_service.repository.UserRepository;
import com.digisecurebank.auth_service.request.LoginRequest;
import com.digisecurebank.auth_service.request.RegisterRequest;
import com.digisecurebank.auth_service.response.LoginResponse;
import com.digisecurebank.auth_service.response.RegisterResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsServiceImplementation userDetailsService;
    private final JWTProvider jwtProvider;


    private Authentication authenticateUser(String email, String password) {
        UserDetails details = userDetailsService.loadUserByUsername(email);

        // if(details == null){
        // throw new BadCredentialsException("Invalid credentials");
        // }

        if (!passwordEncoder.matches(password, details.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        return new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
    }


    public RegisterResponse register(RegisterRequest request){
        boolean userExists = userRepository.existsByEmail(request.getEmail());
        if (userExists) {
            throw new ConflictingResourcesException("User already exists");
        }
        String requestedRole = request.getRole() == null ? null : request.getRole().toString();

        if (requestedRole == null || requestedRole.isEmpty()) {
            requestedRole = "ROLE_USER";
        }
        else if (!requestedRole.equals("ROLE_USER") && !requestedRole.equals("ROLE_ADMIN")) {
            throw new BadRequestException("Invalid role");
        }
        Users user = new Users();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRoles.valueOf(requestedRole));

        Users savedUser = userRepository.save(user);
        UsersDTO savedUserDTO = UserMapper.mapToUsersDTO(savedUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(savedUserDTO.getEmail(),
                savedUserDTO.getPassword());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        return new RegisterResponse(accessToken, refreshToken, savedUserDTO);
    }

    public LoginResponse login(LoginRequest request){

        boolean isUserExists = userRepository.existsByEmail(request.getEmail());
        if (!isUserExists) {
            throw new UnauthorizedAccessException("User with email " + request.getEmail() + " does not exist");
        }

        String email = request.getEmail();
        String password = request.getPassword();

        Authentication authentication = authenticateUser(email, password);

        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        LoginResponse response = new LoginResponse(accessToken, refreshToken);

        return response;

    }


}
