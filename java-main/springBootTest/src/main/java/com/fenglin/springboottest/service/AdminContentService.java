package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.AdminStatsVO;
import com.fenglin.springboottest.dto.CategoryOptionVO;
import com.fenglin.springboottest.dto.ContentForm;
import com.fenglin.springboottest.dto.PageData;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.mapper.CategoryMapper;
import com.fenglin.springboottest.mapper.ContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 后台内容管理业务：门户内容（t_content）的增删改查、上/下架、统计与分类选项
 */
@Service
public class AdminContentService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /** 内容列表（分页 + 关键词 + 分类 + 状态过滤） */
    public PageData<Content> list(String keyword, Long categoryId, Integer status, int page, int size) {
        int pg = Math.max(page, 1);
        int sz = Math.max(size, 1);

        LambdaQueryWrapper<Content> w = Wrappers.lambdaQuery(Content.class);
        if (keyword != null && !keyword.isBlank()) {
            String like = keyword.trim();
            w.and(ww -> ww.like(Content::getTitle, like)
                    .or().like(Content::getSummary, like)
                    .or().like(Content::getTags, like));
        }
        if (categoryId != null) {
            w.eq(Content::getCategoryId, categoryId);
        }
        if (status != null) {
            w.eq(Content::getStatus, status);
        }
        w.orderByDesc(Content::getCreatedAt);

        long total = contentMapper.selectCount(w);
        int from = (pg - 1) * sz;
        w.last("LIMIT " + from + ", " + sz);
        List<Content> records = contentMapper.selectList(w);

        return new PageData<>(records, total, pg, sz);
    }

    /** 内容详情 */
    public Content get(Long id) {
        Content c = contentMapper.selectById(id);
        if (c == null) {
            throw new BizException(404, "内容不存在");
        }
        return c;
    }

    /** 新增内容 */
    public Long create(ContentForm form) {
        Content c = new Content();
        c.setTitle(form.getTitle());
        c.setSummary(form.getSummary() == null ? "" : form.getSummary());
        c.setCoverUrl(form.getCoverUrl());
        c.setContent(form.getContent());
        c.setAuthor(form.getAuthor());
        c.setTags(form.getTags());
        c.setStatus(form.getStatus() == null ? 1 : (form.getStatus() == 1 ? 1 : 0));
        c.setViewCount(0);
        resolveTopCategory(c, form.getCategoryId());
        contentMapper.insert(c);
        return c.getId();
    }

    /** 编辑内容（仅覆盖表单中传入的字段，其余保留原值） */
    public void update(Long id, ContentForm form) {
        Content existing = get(id);
        if (form.getTitle() != null) existing.setTitle(form.getTitle());
        if (form.getSummary() != null) existing.setSummary(form.getSummary());
        if (form.getCoverUrl() != null) existing.setCoverUrl(form.getCoverUrl());
        if (form.getContent() != null) existing.setContent(form.getContent());
        if (form.getAuthor() != null) existing.setAuthor(form.getAuthor());
        if (form.getTags() != null) existing.setTags(form.getTags());
        if (form.getStatus() != null) existing.setStatus(form.getStatus() == 1 ? 1 : 0);
        if (form.getCategoryId() != null) {
            existing.setCategoryId(form.getCategoryId());
            resolveTopCategory(existing, form.getCategoryId());
        }
        contentMapper.updateById(existing);
    }

    /** 删除内容 */
    public void remove(Long id) {
        get(id);
        contentMapper.deleteById(id);
    }

    /** 切换发布 / 下架状态 */
    public void setStatus(Long id, int status) {
        Content c = get(id);
        c.setStatus(status == 1 ? 1 : 0);
        contentMapper.updateById(c);
    }

    /** 概览统计 */
    public AdminStatsVO stats() {
        AdminStatsVO vo = new AdminStatsVO();
        vo.setTotal(contentMapper.selectCount(null));
        vo.setPublished(contentMapper.selectCount(
                Wrappers.lambdaQuery(Content.class).eq(Content::getStatus, 1)));
        vo.setDraft(contentMapper.selectCount(
                Wrappers.lambdaQuery(Content.class).eq(Content::getStatus, 0)));
        vo.setCategoryCount(categoryMapper.selectCount(null));
        return vo;
    }

    /** 内容表单用分类选项（仅叶子分类，带所属一级分类名） */
    public List<CategoryOptionVO> categoryOptions() {
        List<Category> all = categoryMapper.selectList(null);
        Map<Long, Category> byId = all.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity(), (a, b) -> a));

        List<CategoryOptionVO> opts = new ArrayList<>();
        for (Category c : all) {
            if (c.getLevel() != null && c.getLevel() == 2) {
                CategoryOptionVO o = new CategoryOptionVO();
                o.setId(c.getId());
                o.setName(c.getName());
                Category top = byId.get(c.getParentId());
                o.setTopId(top == null ? 0L : top.getId());
                o.setTopName(top == null ? "" : top.getName());
                opts.add(o);
            }
        }
        return opts;
    }

    /** 根据叶子分类解析一级分类（冗余写入 top_category_id） */
    private void resolveTopCategory(Content c, Long leafId) {
        Category cat = categoryMapper.selectById(leafId);
        if (cat == null) {
            throw new BizException(400, "分类不存在");
        }
        c.setCategoryId(cat.getId());
        c.setTopCategoryId(cat.getParentId() == null ? 0L : cat.getParentId());
    }
}
