package com.fenglin.springboottest.dto;

/**
 * 后台概览统计
 */
public class AdminStatsVO {

    /** 内容总数 */
    private long total;

    /** 已发布数（status = 1） */
    private long published;

    /** 草稿 / 下架数（status = 0） */
    private long draft;

    /** 分类总数 */
    private long categoryCount;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public long getDraft() {
        return draft;
    }

    public void setDraft(long draft) {
        this.draft = draft;
    }

    public long getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(long categoryCount) {
        this.categoryCount = categoryCount;
    }
}
