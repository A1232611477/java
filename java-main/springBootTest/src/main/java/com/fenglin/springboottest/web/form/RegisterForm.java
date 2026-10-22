package com.fenglin.springboottest.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 服务器端注册页的表单对象（绑定 templates/site/register.html）
 *
 * <p>和 REST 接口的 {@code dto/RegisterRequest} 分开：页面表单多了「确认密码」，
 * 而且需要中文校验提示直接回显到输入框下面。</p>
 */
public class RegisterForm {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称不能超过 50 个字符")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$",
            message = "密码需 8-20 位且同时包含字母和数字")
    private String password;

    @NotBlank(message = "请再次输入密码")
    private String confirmPassword;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
