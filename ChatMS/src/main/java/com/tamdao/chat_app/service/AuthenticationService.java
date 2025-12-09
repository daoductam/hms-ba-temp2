package com.tamdao.chat_app.service;

import com.tamdao.chat_app.dto.LoginRequestDTO;
import com.tamdao.chat_app.dto.LoginResponseDTO;
import com.tamdao.chat_app.dto.RegisterRequestDTO;
import com.tamdao.chat_app.dto.UserDTO;
import com.tamdao.chat_app.model.User;
import com.tamdao.chat_app.repository.UserRepository;
import com.tamdao.chat_app.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public UserDTO signup(RegisterRequestDTO registerRequestDTO) {
        if (userRepository.findByUsername(registerRequestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username is already in use");
        }

        User user = User.builder()
                .username(registerRequestDTO.getUsername())
                .password(passwordEncoder.encode(registerRequestDTO.getPassword()))
                .email(registerRequestDTO.getEmail())
                .build();

        User savedUser = userRepository.save(user);
        return convertToUserDTO(user);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {

        User user = userRepository.findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Username not found"));
        log.info("Tim dc r");
        log.info("bat dau xac thuc");
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDTO.getUsername(),
                loginRequestDTO.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("xac thuc thanh cong");
        String jwtToken = jwtService.generateToken(user);
        log.info("gui response");
        return LoginResponseDTO.builder()
                .token(jwtToken)
                .userDTO(convertToUserDTO(user))
                .build();
    }

    public ResponseEntity<String> logout() {

        ResponseCookie responseCookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body("Logged out successfully");
    }

    public Map<String, Object> getOnlineUsers() {
        List<User> usersList = userRepository.findByIsOnlineTrue();
        Map<String, Object> onlineUsers = usersList.stream()
                .collect(Collectors.toMap(User::getUsername, user -> user));
        return onlineUsers;
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setId(user.getId());

        return userDTO;
    }
}
