package com.fenglin.springboottest.web.form;

import jakarta.validation.constraints.NotBlank;

/**
 * 服务器端登录页的表单对象（绑定 templates/site/login.html）
 *
 * <p>账号 = 注册时填写的昵称。</p>
 */
public class LoginForm {

    @NotBlank(message = "账号不能为空")
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
