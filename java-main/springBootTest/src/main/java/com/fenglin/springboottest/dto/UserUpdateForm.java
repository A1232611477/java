package com.fenglin.springboottest.dto;

/**
 * 后台编辑账号表单（仅覆盖传入的非空字段）
 */
public class UserUpdateForm {

    /** 新昵称（选填，传了才改） */
    private String nickname;

    /** 新角色（选填，只能 USER / ADMIN） */
    private String role;

    /** 新状态（选填，0 正常 / 1 禁用） */
    private Integer status;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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
