package com.fernando.microservices.auth_service.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fernando.microservices.auth_service.entity.UserInfo;
import com.fernando.microservices.auth_service.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userRepository.findByEmail(username)
            .orElseThrow(() -> new RuntimeException("User not found by that email"));

        return UserDetailsImpl.build(userInfo);
    }
    
}
