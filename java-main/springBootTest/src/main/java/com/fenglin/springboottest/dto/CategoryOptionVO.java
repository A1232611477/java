package com.fenglin.springboottest.dto;

/**
 * 内容表单用的分类选项（仅叶子分类，带所属一级分类名，便于前端分组展示）
 */
public class CategoryOptionVO {

    /** 叶子（二级）分类 id */
    private Long id;

    /** 叶子分类名称 */
    private String name;

    /** 所属一级分类 id */
    private Long topId;

    /** 所属一级分类名称 */
    private String topName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTopId() {
        return topId;
    }

    public void setTopId(Long topId) {
        this.topId = topId;
    }

    public String getTopName() {
        return topName;
    }

    public void setTopName(String topName) {
        this.topName = topName;
    }
}
