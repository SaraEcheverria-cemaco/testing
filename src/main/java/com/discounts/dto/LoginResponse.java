package com.discounts.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private boolean vip;

    public LoginResponse(String token, String username, boolean vip) {
        this.token = token;
        this.username = username;
        this.vip = vip;
    }
}
