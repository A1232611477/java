package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求参数
 */
public class LoginRequest {

    /** 账号 = 注册时填写的昵称 */
    @NotBlank(message = "账号不能为空")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 是否勾选「记住我」：true 时签发长效令牌（有效期见 jwt.remember-expire-minutes，默认 7 天）。
     * 可以不传，等同于 false，所以不加 @NotNull。
     */
    private Boolean rememberMe;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    /** 空安全地读取，前端不传该字段时不会 NPE */
    public boolean isRememberMe() {
        return Boolean.TRUE.equals(rememberMe);
    }
}
