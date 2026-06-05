package com.wjb.auth.dto;

import java.util.List;

public class UserInfoResponse {
    private Long userId;
    private String username;
    private String nickname;
    private List<String> roles;
    private List<String> perms;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public List<String> getPerms() { return perms; }
    public void setPerms(List<String> perms) { this.perms = perms; }
}
