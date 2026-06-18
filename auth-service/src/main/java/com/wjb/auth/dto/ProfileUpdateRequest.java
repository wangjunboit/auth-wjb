package com.wjb.auth.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
}
