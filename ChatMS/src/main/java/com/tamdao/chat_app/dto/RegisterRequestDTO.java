package com.tamdao.chat_app.dto;

import lombok.Data;

@Data
public class RegisterRequestDTO {

    private String username;
    private String password;
    private String email;
}
