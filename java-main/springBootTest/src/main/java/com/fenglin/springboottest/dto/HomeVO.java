package com.fenglin.springboottest.dto;

import java.util.List;

/**
 * 首页展示数据：分级分类树 + 精选内容
 */
public class HomeVO {

    private List<CategoryNodeVO> categories;
    private List<ContentSummaryVO> featured;

    public List<CategoryNodeVO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryNodeVO> categories) {
        this.categories = categories;
    }

    public List<ContentSummaryVO> getFeatured() {
        return featured;
    }

    public void setFeatured(List<ContentSummaryVO> featured) {
        this.featured = featured;
    }
}
