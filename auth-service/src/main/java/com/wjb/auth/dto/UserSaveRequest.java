package com.wjb.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** 新增/修改用户请求;修改时 id 必填,新增时 password 必填 */
public class UserSaveRequest {
    private Long id;
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
