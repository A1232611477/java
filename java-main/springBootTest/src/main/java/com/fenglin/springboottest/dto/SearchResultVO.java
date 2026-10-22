package com.fenglin.springboottest.dto;

import java.util.List;

/**
 * 搜索结果（支持分级分类过滤 + 分页）
 */
public class SearchResultVO {

    private long total;
    private int page;
    private int size;
    private List<ContentSummaryVO> list;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<ContentSummaryVO> getList() {
        return list;
    }

    public void setList(List<ContentSummaryVO> list) {
        this.list = list;
    }
}
