package com.wjb.auth.oauth;

import com.wjb.auth.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** GitHub 真实 OAuth */
@Component
public class GitHubOAuthProvider implements OAuthProvider {

    @Value("${oauth.github.client-id}")
    private String clientId;
    @Value("${oauth.github.client-secret}")
    private String clientSecret;
    @Value("${oauth.github.redirect-uri}")
    private String redirectUri;
    @Value("${oauth.github.authorize-url}")
    private String authorizeUrl;
    @Value("${oauth.github.token-url}")
    private String tokenUrl;
    @Value("${oauth.github.user-url}")
    private String userUrl;

    private final RestClient rest = RestClient.create();

    @Override
    public String provider() {
        return "github";
    }

    @Override
    public String authorizeUrl(String state) {
        String redirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return authorizeUrl + "?client_id=" + clientId
                + "&redirect_uri=" + redirect
                + "&scope=read:user&state=" + state;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuthUser fetchUser(String code) {
        // 1. code 换 access_token
        String body = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&redirect_uri=" + redirectUri;
        Map<String, Object> tokenResp = rest.post().uri(tokenUrl)
                .header("Accept", "application/json")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve().body(Map.class);
        if (tokenResp == null || tokenResp.get("access_token") == null) {
            throw new ServiceException("GitHub 授权失败");
        }
        String accessToken = String.valueOf(tokenResp.get("access_token"));
        // 2. 拉用户信息
        Map<String, Object> user = rest.get().uri(userUrl)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve().body(Map.class);
        if (user == null || user.get("id") == null) {
            throw new ServiceException("获取 GitHub 用户信息失败");
        }
        return new OAuthUser("github",
                String.valueOf(user.get("id")),
                (String) user.get("login"),
                (String) user.get("avatar_url"));
    }
}
