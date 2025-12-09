package com.tamdao.chat_app.controller;

import com.tamdao.chat_app.dto.LoginRequestDTO;
import com.tamdao.chat_app.dto.LoginResponseDTO;
import com.tamdao.chat_app.dto.RegisterRequestDTO;
import com.tamdao.chat_app.dto.UserDTO;
import com.tamdao.chat_app.model.User;
import com.tamdao.chat_app.repository.UserRepository;
import com.tamdao.chat_app.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody RegisterRequestDTO registerRequestDTO) {
        return ResponseEntity.ok(authenticationService.signup(registerRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login (@RequestBody LoginRequestDTO loginRequestDTO) {

        LoginResponseDTO loginResponseDTO = authenticationService.login(loginRequestDTO);
        ResponseCookie responseCookie = ResponseCookie.from("JWT", loginResponseDTO.getToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(1*60*60)
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginResponseDTO.getUserDTO());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return authenticationService.logout();
    }

    @GetMapping("/getonlineusers")
    public ResponseEntity<Map<String, Object>> getOnlineUses() {
        return ResponseEntity.ok(authenticationService.getOnlineUsers());
    }

    @GetMapping("/getcurrentuser")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        if (authentication==null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("USER NOT AUTHORIZED");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(convertToUserDTO(user));
    }

    public UserDTO convertToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(user.getEmail());
        userDTO.setUsername(user.getUsername());
        userDTO.setId(user.getId());

        return userDTO;
    }
}
