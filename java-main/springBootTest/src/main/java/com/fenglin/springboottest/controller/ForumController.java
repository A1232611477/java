package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.PageData;
import com.fenglin.springboottest.dto.PostVO;
import com.fenglin.springboottest.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.fenglin.springboottest.interceptor.JwtInterceptor.ATTR_USER_ID;

/**
 * 论坛接口：发帖 / 列表 / 详情 / 删除（需要登录令牌）
 */
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    /** 帖子列表：GET /api/forum?page=1&size=20 */
    @GetMapping
    public Result<PageData<PostVO>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(forumService.list(page, size));
    }

    /** 发帖：POST /api/forum { title, content } */
    @PostMapping
    public Result<Long> create(@RequestAttribute(ATTR_USER_ID) Long userId,
                               @RequestBody Map<String, String> body) {
        String title = body == null ? null : body.get("title");
        String content = body == null ? null : body.get("content");
        return Result.success(forumService.create(userId, title, content));
    }

    /** 帖子详情：GET /api/forum/{id} */
    @GetMapping("/{id}")
    public Result<PostVO> detail(@PathVariable Long id) {
        return Result.success(forumService.detail(id));
    }

    /** 删除帖子（仅作者）：DELETE /api/forum/{id} */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@RequestAttribute(ATTR_USER_ID) Long userId,
                               @PathVariable Long id) {
        forumService.remove(id, userId);
        return Result.success();
    }
}
