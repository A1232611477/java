package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 注册请求参数
 */
public class RegisterRequest {

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    private String password;

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
}
