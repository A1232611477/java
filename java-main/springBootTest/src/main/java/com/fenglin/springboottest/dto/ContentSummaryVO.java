package com.fenglin.springboottest.dto;

import java.time.LocalDateTime;

/**
 * 内容摘要（列表/搜索结果/首页精选展示用，不含正文）
 */
public class ContentSummaryVO {

    private Long id;
    /** 标题 */
    private String title;
    /** 一级分类名 */
    private String category;
    /** 二级（叶子）分类名 */
    private String subCategory;
    /** 摘要（兼容前端 early 版本字段名 desc） */
    private String desc;
    /** 封面图 */
    private String coverUrl;
    /** 视频嵌入地址（B站 iframe src 或 BV 链接） */
    private String videoUrl;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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
