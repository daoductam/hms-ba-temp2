package com.tamdao.chat_app.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class UserDTO {
    private long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false, name = "is_online")
    private boolean isOnline;
}
