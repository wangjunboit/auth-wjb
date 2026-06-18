package com.wjb.auth.dto;

import lombok.Data;

@Data
public class ProfileVO {
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
}
