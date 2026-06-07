package com.contest.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String name;
    private String role;
    private String email;
    private String studentNo;
}
