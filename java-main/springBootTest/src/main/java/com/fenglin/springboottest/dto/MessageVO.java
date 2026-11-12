package com.fenglin.springboottest.dto;

import com.fenglin.springboottest.entity.Message;

import java.time.LocalDateTime;

/**
 * 私信消息对外 VO（附带发送方昵称，便于前端直接展示）
 */
public class MessageVO {

    private Long id;
    private Long senderId;
    private String senderNickname;
    private Long receiverId;
    private String content;
    private Integer readFlag;
    private LocalDateTime createdAt;

    public static MessageVO from(Message m, String senderNickname) {
        MessageVO vo = new MessageVO();
        vo.setId(m.getId());
        vo.setSenderId(m.getSenderId());
        vo.setSenderNickname(senderNickname);
        vo.setReceiverId(m.getReceiverId());
        vo.setContent(m.getContent());
        vo.setReadFlag(m.getReadFlag());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(Integer readFlag) {
        this.readFlag = readFlag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
