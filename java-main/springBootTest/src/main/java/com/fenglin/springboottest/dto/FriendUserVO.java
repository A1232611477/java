package com.fenglin.springboottest.dto;

import com.fenglin.springboottest.entity.User;

/**
 * 对外返回的「好友 / 用户」简要信息（含昵称与状态，不含密码）
 */
public class FriendUserVO {

    private Long id;
    private String nickname;
    private Integer status;
    private String role;
    private java.time.LocalDateTime createdAt;

    public static FriendUserVO from(User user) {
        FriendUserVO vo = new FriendUserVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setStatus(user.getStatus());
        vo.setRole(user.getRole());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
