package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.dto.CategoryNodeVO;
import com.fenglin.springboottest.dto.ContentSummaryVO;
import com.fenglin.springboottest.dto.HomeVO;
import com.fenglin.springboottest.dto.SearchResultVO;
import com.fenglin.springboottest.entity.Category;
import com.fenglin.springboottest.entity.Content;
import com.fenglin.springboottest.mapper.CategoryMapper;
import com.fenglin.springboottest.mapper.ContentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 门户首页 & 搜索业务：
 * <ul>
 *   <li>首页展示：分级分类树 + 精选内容</li>
 *   <li>搜索：关键词 + 分级分类（一级/二级）过滤 + 分页</li>
 * </ul>
 */
@Service
public class HomeService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ContentMapper contentMapper;

    /** 首页展示数据 */
    public HomeVO getHome() {
        HomeVO vo = new HomeVO();
        vo.setCategories(buildCategoryTree());
        vo.setFeatured(getFeatured(6));
        return vo;
    }

    /**
     * 分级分类搜索
     *
     * @param q             关键词（匹配标题/摘要/标签）
     * @param categoryId    一级分类 id（按大类过滤）
     * @param subCategoryId 二级（叶子）分类 id（按小类过滤）
     * @param page          页码（从 1 开始）
     * @param size          每页条数
     */
    public SearchResultVO search(String q, Long categoryId, Long subCategoryId, int page, int size) {
        int pg = Math.max(page, 1);
        int sz = Math.max(size, 1);

        LambdaQueryWrapper<Content> w = Wrappers.lambdaQuery(Content.class)
                .eq(Content::getStatus, 1);

        if (q != null && !q.isBlank()) {
            String like = q.trim();
            w.and(ww -> ww.like(Content::getTitle, like)
                    .or().like(Content::getSummary, like)
                    .or().like(Content::getTags, like));
        }
        if (categoryId != null) {
            w.eq(Content::getTopCategoryId, categoryId);
        }
        if (subCategoryId != null) {
            w.eq(Content::getCategoryId, subCategoryId);
        }
        w.orderByDesc(Content::getCreatedAt);

        // 手动分页：先 count 再 limit（不依赖分页插件，兼容性更好）
        long total = contentMapper.selectCount(w);
        int from = (pg - 1) * sz;
        w.last("LIMIT " + from + ", " + sz);
        List<Content> records = contentMapper.selectList(w);

        Map<Long, String> catName = categoryNameMap();
        SearchResultVO res = new SearchResultVO();
        res.setTotal(total);
        res.setPage(pg);
        res.setSize(sz);
        res.setList(records.stream()
                .map(c -> toSummary(c, catName))
                .collect(Collectors.toList()));
        return res;
    }

    /** 构建分级分类树（一级 + 二级） */
    private List<CategoryNodeVO> buildCategoryTree() {
        List<Category> all = categoryMapper.selectList(null);
        List<CategoryNodeVO> tree = new ArrayList<>();
        for (Category c : all) {
            if (c.getLevel() != null && c.getLevel() == 1) {
                CategoryNodeVO node = new CategoryNodeVO();
                node.setId(c.getId());
                node.setName(c.getName());
                node.setCode(c.getCode());
                node.setChildren(new ArrayList<>());
                tree.add(node);
            }
        }
        for (Category c : all) {
            if (c.getLevel() != null && c.getLevel() == 2) {
                CategoryNodeVO child = new CategoryNodeVO();
                child.setId(c.getId());
                child.setName(c.getName());
                child.setCode(c.getCode());
                tree.stream()
                        .filter(t -> t.getId().equals(c.getParentId()))
                        .findFirst()
                        .ifPresent(t -> t.getChildren().add(child));
            }
        }
        tree.sort(Comparator.comparing(CategoryNodeVO::getId));
        return tree;
    }

    /** 最新发布的精选内容 */
    private List<ContentSummaryVO> getFeatured(int n) {
        List<Content> list = contentMapper.selectList(
                Wrappers.lambdaQuery(Content.class)
                        .eq(Content::getStatus, 1)
                        .orderByDesc(Content::getCreatedAt)
                        .last("LIMIT " + n));
        Map<Long, String> catName = categoryNameMap();
        return list.stream().map(c -> toSummary(c, catName)).collect(Collectors.toList());
    }

    private Map<Long, String> categoryNameMap() {
        return categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    private ContentSummaryVO toSummary(Content c, Map<Long, String> catName) {
        ContentSummaryVO v = new ContentSummaryVO();
        v.setId(c.getId());
        v.setTitle(c.getTitle());
        v.setDesc(c.getSummary());
        v.setCoverUrl(c.getCoverUrl());
        v.setAuthor(c.getAuthor());
        v.setTags(c.getTags());
        v.setViewCount(c.getViewCount());
        v.setCreatedAt(c.getCreatedAt());
        v.setCategory(catName.getOrDefault(c.getTopCategoryId(), ""));
        v.setSubCategory(catName.getOrDefault(c.getCategoryId(), ""));
        return v;
    }
}
