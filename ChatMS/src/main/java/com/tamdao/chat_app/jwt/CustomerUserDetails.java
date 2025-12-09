package com.tamdao.chat_app.jwt;

import com.tamdao.chat_app.model.User;
import com.tamdao.chat_app.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class CustomerUserDetails implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Damg load user {}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Username not found"));

            log.info("Da load user {}", username);

            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(Collections.emptyList())
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        } catch (Exception e) {
            log.error("ko load dc {}", e.getMessage(),e);
        }
        log.info("ko load user {}", username);
        return null;


    }
}
