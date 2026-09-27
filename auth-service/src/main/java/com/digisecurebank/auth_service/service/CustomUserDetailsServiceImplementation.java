package com.digisecurebank.auth_service.service;

import com.digisecurebank.auth_service.entity.Users;
import com.digisecurebank.auth_service.exception.ConflictingResourcesException;
import com.digisecurebank.auth_service.repository.UserRepository;
import com.digisecurebank.auth_service.request.RegisterRequest;
import com.digisecurebank.auth_service.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class, UsernameNotFoundException.class})
public class CustomUserDetailsServiceImplementation implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().toString());

        Collection<? extends GrantedAuthority> authorities = java.util.Collections.singletonList(authority);

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }
}
