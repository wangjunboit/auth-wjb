package com.wjb.auth.oauth;

/** 第三方 OAuth 提供方抽象 */
public interface OAuthProvider {

    /** 平台标识:github / wechat */
    String provider();

    /** 拼授权跳转地址 */
    String authorizeUrl(String state);

    /** 用授权 code 换取第三方用户信息 */
    OAuthUser fetchUser(String code);
}
