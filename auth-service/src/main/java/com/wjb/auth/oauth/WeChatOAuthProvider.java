package com.wjb.auth.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 微信 mock:不接真实微信,固定返回一个 openId,便于演示 */
@Component
public class WeChatOAuthProvider implements OAuthProvider {

    @Value("${oauth.github.redirect-uri}")
    private String redirectUri;

    @Override
    public String provider() {
        return "wechat";
    }

    @Override
    public String authorizeUrl(String state) {
        // mock:直接回跳前端回调页,带一个假 code
        return redirectUri + "?code=mock_wechat_code&state=" + state;
    }

    @Override
    public OAuthUser fetchUser(String code) {
        // mock:固定用户
        return new OAuthUser("wechat", "wx_mock_001", "微信用户", null);
    }
}
