package com.fenglin.springboottest.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.PageData;
import com.fenglin.springboottest.dto.PostVO;
import com.fenglin.springboottest.entity.Post;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.PostMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 论坛业务：帖子的发布、分页列表、详情、删除
 */
@Service
public class ForumService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    /** 发帖 */
    public Long create(Long authorId, String title, String content) {
        if (title == null || title.isBlank()) {
            throw new BizException(400, "标题不能为空");
        }
        if (title.trim().length() > 120) {
            throw new BizException(400, "标题过长（最多 120 字）");
        }
        Post p = new Post();
        p.setAuthorId(authorId);
        p.setTitle(title.trim());
        p.setContent(content == null ? "" : content.trim());
        postMapper.insert(p);
        return p.getId();
    }

    /** 帖子列表（按时间倒序分页），附带作者昵称 */
    public PageData<PostVO> list(int page, int size) {
        int pg = Math.max(page, 1);
        int sz = Math.max(size, 1);

        long total = postMapper.selectCount(null);
        int from = (pg - 1) * sz;
        List<Post> posts = postMapper.selectList(Wrappers.lambdaQuery(Post.class)
                .orderByDesc(Post::getId)
                .last("LIMIT " + from + ", " + sz));

        List<PostVO> list = posts.stream()
                .map(p -> {
                    User u = userMapper.selectById(p.getAuthorId());
                    return PostVO.from(p, u == null ? "已注销" : u.getNickname());
                })
                .collect(Collectors.toList());
        return new PageData<>(list, total, pg, sz);
    }

    /** 帖子详情 */
    public PostVO detail(Long id) {
        Post p = postMapper.selectById(id);
        if (p == null) {
            throw new BizException(404, "帖子不存在或已删除");
        }
        User u = userMapper.selectById(p.getAuthorId());
        return PostVO.from(p, u == null ? "已注销" : u.getNickname());
    }

    /** 删除帖子（仅作者本人可删） */
    public void remove(Long id, Long operatorId) {
        Post p = postMapper.selectById(id);
        if (p == null) {
            throw new BizException(404, "帖子不存在或已删除");
        }
        if (!p.getAuthorId().equals(operatorId)) {
            throw new BizException(403, "只能删除自己发布的帖子");
        }
        postMapper.deleteById(id);
    }

    /** 论坛概览（帖子总数） */
    public Map<String, Object> stats() {
        Map<String, Object> map = new HashMap<>();
        map.put("total", postMapper.selectCount(null));
        return map;
    }
}
