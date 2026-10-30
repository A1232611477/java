package com.fenglin.springboottest.dto;

import java.time.LocalDateTime;

/**
 * 内容详情（含正文 content，供攻略/资讯等详情弹窗展示）
 */
public class ContentDetailVO {

    private Long id;
    /** 标题 */
    private String title;
    /** 一级分类名 */
    private String category;
    /** 二级（叶子）分类名 */
    private String subCategory;
    /** 摘要 */
    private String summary;
    /** 封面图 */
    private String coverUrl;
    /** 视频嵌入地址（B站 iframe src 或 BV 链接） */
    private String videoUrl;
    /** 正文（文字讲解） */
    private String content;
    /** 作者 / 来源 */
    private String author;
    /** 标签（逗号分隔） */
    private String tags;
    /** 浏览量 */
    private Integer viewCount;
    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
