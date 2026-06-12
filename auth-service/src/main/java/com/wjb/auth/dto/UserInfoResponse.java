package com.wjb.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String nickname;
    private List<String> roles;
    private List<String> perms;
}
