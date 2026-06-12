package com.wjb.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OAuthBindingVO {
    private String provider;
    private String openId;
}
