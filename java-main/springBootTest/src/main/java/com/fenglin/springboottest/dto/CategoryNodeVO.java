package com.fenglin.springboottest.dto;

import java.util.List;

/**
 * 分类节点（用于构建分级分类树）
 */
public class CategoryNodeVO {

    private Long id;
    private String name;
    private String code;
    private List<CategoryNodeVO> children;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<CategoryNodeVO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryNodeVO> children) {
        this.children = children;
    }
}
