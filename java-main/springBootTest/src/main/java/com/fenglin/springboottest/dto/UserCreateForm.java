package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 后台创建账号表单
 */
public class UserCreateForm {

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[\\w!@#$%^&*.]{8,20}$", message = "密码需 8-20 位，可含字母、数字及 !@#$%^&*.")
    private String password;

    /** 角色：USER / ADMIN，缺省 USER */
    private String role;

    /** 状态：0 正常，1 禁用，缺省 0 */
    private Integer status;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
