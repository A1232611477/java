package com.fenglin.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 内容新增 / 编辑 表单
 *
 * <p>新增与编辑共用同一结构；编辑时未传的字段服务端会保留原值（见 AdminContentService）。</p>
 */
public class ContentForm {

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 120, message = "标题不超过 120 字")
    private String title;

    /** 摘要 */
    @Size(max = 255, message = "摘要不超过 255 字")
    private String summary;

    /** 叶子（二级）分类 id */
    @NotNull(message = "请选择分类")
    private Long categoryId;

    /** 封面图 URL */
    private String coverUrl;

    /** 视频嵌入地址（B站 iframe src 或 BV 链接） */
    private String videoUrl;

    /** 正文 */
    private String content;

    /** 作者 / 来源 */
    private String author;

    /** 标签（逗号分隔） */
    private String tags;

    /** 状态：0 下架/草稿，1 已发布 */
    private Integer status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
