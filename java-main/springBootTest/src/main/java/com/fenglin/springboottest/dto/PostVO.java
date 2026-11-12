package com.fenglin.springboottest.dto;

import com.fenglin.springboottest.entity.Post;

import java.time.LocalDateTime;

/**
 * 论坛帖子对外 VO（附带作者昵称）
 */
public class PostVO {

    private Long id;
    private Long authorId;
    private String authorNickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public static PostVO from(Post p, String authorNickname) {
        PostVO vo = new PostVO();
        vo.setId(p.getId());
        vo.setAuthorId(p.getAuthorId());
        vo.setAuthorNickname(authorNickname);
        vo.setTitle(p.getTitle());
        vo.setContent(p.getContent());
        vo.setCreatedAt(p.getCreatedAt());
        return vo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
