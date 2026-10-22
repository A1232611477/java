package com.fenglin.springboottest.dto;

/**
 * 登录成功返回：令牌 + 有效期 + 用户信息
 */
public class LoginResponse {

    /** 访问令牌，前端需放入请求头 Authorization: Bearer {token} */
    private String token;

    /** 固定为 Bearer，方便前端拼接请求头 */
    private String tokenType;

    /** 有效期（秒） */
    private long expiresIn;

    private UserVO user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
