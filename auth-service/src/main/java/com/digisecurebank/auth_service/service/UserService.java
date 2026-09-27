package com.digisecurebank.auth_service.service;


import com.digisecurebank.auth_service.dto.UsersDTO;
import com.digisecurebank.auth_service.entity.Users;
import com.digisecurebank.auth_service.exception.ObjectNotFoundException;
import com.digisecurebank.auth_service.exception.UnauthorizedAccessException;
import com.digisecurebank.auth_service.jwt.JWTProvider;
import com.digisecurebank.auth_service.mapper.UserMapper;
import com.digisecurebank.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = {Exception.class, ObjectNotFoundException.class})
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JWTProvider provider;

    public UsersDTO getUserFromAuth(String jwt){
        if (jwt == null || jwt.trim().isEmpty()) {
            throw new UnauthorizedAccessException("JWT token is empty");
        }

        try {
            String email = provider.getEmailFromToken(jwt);
            Users user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ObjectNotFoundException("User not found"));
            return UserMapper.mapToUsersDTO(user);
        } catch (Exception e) {
            if (e instanceof ObjectNotFoundException) {
                throw e;
            }
            throw new UnauthorizedAccessException("Invalid or expired JWT token");
        }
    }


}
