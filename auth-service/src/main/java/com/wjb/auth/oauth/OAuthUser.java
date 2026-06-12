package com.wjb.auth.oauth;

/** 第三方用户信息 */
public record OAuthUser(String provider, String openId, String nickname, String avatar) {
}
